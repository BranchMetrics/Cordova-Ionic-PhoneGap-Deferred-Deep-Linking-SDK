(function() {
  // properties

  const fs = require("fs");
  const plist = require("plist");
  const SDK = "branch-cordova-sdk";

  // entry
  module.exports = {
    addBranchSettings: addBranchSettings
  };

  // updates the platforms/ios/app.plist file with branch settings within app/config.xml
  function addBranchSettings(preferences) {
  	const projectName = preferences.projectName.replace( /[\r\n]+/gm, '' ).replace(/(^[\s]+|[\s]+$)/g, '');
  	const filePath = `platforms/ios/${projectName}/${projectName}-Info.plist`;
    let xml = readPlist(filePath);
    let obj = convertXmlToObject(xml);

    obj = appendPlist(obj, preferences);
    obj = correctPlistBlanks(obj);
    xml = convertObjectToXml(obj);
    writePList(filePath, xml);
  }

  function convertXmlToObject(xml) {
    return plist.parse(xml);
  }

  function convertObjectToXml(obj) {
    return plist.build(obj);
  }

  function readPlist(filePath) {
    return fs.readFileSync(filePath, "utf8");
  }

  function writePList(filePath, xml) {
    return fs.writeFileSync(filePath, xml, { encoding: "utf8" });
  }

  // adds Branch data to pList:
  //    <key>CFBundleURLTypes</key>
  //    <array>
  //      <dict>
  //        <key>CFBundleURLName</key>
  //        <string>branch-cordova-sdk</string>
  //        <key>CFBundleURLSchemes</key>
  //        <array>
  //          <string>enefftest</string>
  //        </array>
  //      </dict>
  //    </array>
  //    <key>branch_key</key>
  //    <string>key_live_icCccJIpd7GlYY5oOmoEtpafuDiuyXhT</string>
  //    <key>branch_app_domain</key>
  //    <string>rawsr.app.link</string>
  function appendPlist(obj, preferences) {
    const urlType = {
      CFBundleURLName: SDK,
      CFBundleURLSchemes: [preferences.uriScheme]
    };
    // ios specific domain will be preferred
    const linkDomains = [...preferences.iosLinkDomain, ...preferences.linkDomain];

    if (!obj.hasOwnProperty("CFBundleURLTypes")) {
      // add
      obj.CFBundleURLTypes = [urlType];
    } else {
      const urls = obj.CFBundleURLTypes;
      let found = false;

      // rename
      for (let i = urls.length - 1; i >= 0; i--) {
        const url = urls[i];
        if (
          url.hasOwnProperty("CFBundleURLName") &&
          url.CFBundleURLName === SDK
        ) {
          url.CFBundleURLSchemes = [preferences.uriScheme];
          found = true;
          break;
        }
      }
      // append
      if (!found) {
        obj.CFBundleURLTypes.push(urlType);
      }
    }

    // override
    if (preferences.branchKeyTest) {
      obj.branch_key = {
        live: preferences.branchKey,
        test: preferences.branchKeyTest
      };
    } else {
      obj.branch_key = preferences.branchKey;
    }
    
    obj.branch_app_domain = linkDomains[0];

    return obj;
  }

  // npm list 1.2.0 -> 2.0.1 causes app crash due to malformed plist (from <string/> to <string></string>)
  // specifically NSMainNibFile and NSMainNibFile~ipad
  function correctPlistBlanks(obj) {
    for (const key in obj) {
      const val = obj[key];
      if (!val) obj[key] = "";
    }

    return obj;
  }
})();
