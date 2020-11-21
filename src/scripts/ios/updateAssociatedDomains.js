(function () {
  // properties

  const path = require("path");
  const fs = require("fs");
  const plist = require("plist");
  const mkpath = require("mkpath");
  // TODO [codinronan 15.01.2020]: Read these from {preferences.projectName}.plist --
  //  most people don't change them, but some do, and more importantly, some people add new ones!
  const BUILD_TYPES = ["Debug", "Release"];
  const ASSOCIATED_DOMAINS = "com.apple.developer.associated-domains";

  // entry
  module.exports = {
    addAssociatedDomains: addAssociatedDomains
  };

  // updates the associated domains from the link-domain field of the app's config.xml
  function addAssociatedDomains(preferences) {
    const files = getEntitlementFiles(preferences);

    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      let entitlements = getEntitlements(file);

      entitlements = updateEntitlements(entitlements, preferences);
      setEntitlements(file, entitlements);
    }
  }

  // get the xcode .entitlements and provisioning profile .plist
  function getEntitlementFiles(preferences) {
    const files = [];

    for (let i = 0; i < BUILD_TYPES.length; i++) {
      const buildType = BUILD_TYPES[i];
      const projectName = preferences.projectName.replace( /[\r\n]+/gm, '' ).replace(/(^[\s]+|[\s]+$)/g, '');
      const plist = path.join(
        preferences.projectRoot,
        "platforms",
        "ios",
        projectName,
        `Entitlements-${buildType}.plist`
      );
      files.push(plist);
    }

    return files;
  }

  // save entitlements
  function setEntitlements(file, entitlements) {
    const plistContent = plist.build(entitlements);

    mkpath.sync(path.dirname(file));

    fs.writeFileSync(file, plistContent, "utf8");
  }

  // read entitlements
  function getEntitlements(file) {
    let content;

    try {
      content = fs.readFileSync(file, "utf8");
    } catch (err) {
      return {};
    }

    return plist.parse(content);
  }

  // appends Branch link domains to the Associated Domain entitlement's file
  //    <dict>
  //      <key>com.apple.developer.associated-domains</key>
  //      <array>
  //        <string>applinks:rawsr.app.link</string>
  //        <string>applinks:rawsr-alternate.app.link</string>
  //      </array>
  //    </dict>
  function updateEntitlements(entitlements, preferences) {
    const domains = [];
    let prev = entitlements[ASSOCIATED_DOMAINS];
    const next = updateAssociatedDomains(preferences);

    prev = removePreviousAssociatedDomains(preferences, prev);
    entitlements[ASSOCIATED_DOMAINS] = domains.concat(prev, next);

    return entitlements;
  }

  // removed previous associated domains related to Branch (will not remove link domain changes from custom domains or custom sub domains)
  function removePreviousAssociatedDomains(preferences, domains) {
    const output = [];
    const linkDomains = [...preferences.iosLinkDomain, ...preferences.linkDomain];

    if (!domains) return output;
    for (let i = 0; i < domains.length; i++) {
      let domain = domains[i];
      if (domain.indexOf("applinks:") === 0) {
        domain = domain.replace("applinks:", "");
        if (isBranchAssociatedDomains(domain, linkDomains)) {
          output.push(`applinks:${domain}`);
        }
      } else if (isBranchAssociatedDomains(domain, linkDomains)) {
        output.push(domain);
      }
    }

    return output;
  }

  // determine if previous associated domain is related to Branch (to prevent duplicates when appending new)
  function isBranchAssociatedDomains(domain, linkDomains) {
    return !(
      domain.indexOf("bnc.lt") > 0 ||
      domain.indexOf("app.link") > 0 ||
      linkDomains.indexOf(domain) >= 0
    );
  }

  // determine which Branch Link Domains to append
  function updateAssociatedDomains(preferences) {
    const domainList = [];
    const prefix = "applinks:";
    const linkDomains = [...preferences.iosLinkDomain, ...preferences.linkDomain];

    for (let i = 0; i < linkDomains.length; i++) {
      const linkDomain = linkDomains[i];

      // add link domain to associated domain
      domainList.push(prefix + linkDomain);

      // app.link link domains need -alternate associated domains as well (for Deep Views)
      if (linkDomain.indexOf("app.link") !== -1) {
        const first = linkDomain.split(".")[0];
        const second = linkDomain.split(".")[1];
        const rest = linkDomain
          .split(".")
          .slice(2)
          .join(".");
        const alternate = `${first}-alternate`;

        domainList.push(`${prefix + alternate}.${second}.${rest}`);
      }
    }

    return domainList;
  }
})();
