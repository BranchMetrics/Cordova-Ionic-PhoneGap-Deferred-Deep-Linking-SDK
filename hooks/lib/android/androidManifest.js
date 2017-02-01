(function () {
  // properties
  'use strict'
  var path = require('path')
  var xmlHelper = require('../sdk/xmlHelper.js')

  // entry
  module.exports = {
    writePreferences: writePreferences
  }

  // injects config.xml preferences into AndroidManifest.xml file.
  function writePreferences (context, preferences) {
    var pathToManifest = path.join(context.opts.projectRoot, 'platforms', 'android', 'AndroidManifest.xml')
    var manifest = xmlHelper.readXmlAsJson(pathToManifest)

    // TODO: early exit

    console.log('BRANCH SDK: Updating AndroidManifest.xml')

    // update manifest
    manifest = updateBranchKeyMetaData(manifest, preferences)
    manifest = updateBranchReferrerTracking(manifest)
    manifest = updateLaunchOptionToSingleTask(manifest, preferences)
    manifest = updateBranchURIScheme(manifest, preferences)
    manifest = updateBranchAppLinks(manifest, preferences)

    // save new version of the AndroidManifest
    xmlHelper.writeJsonAsXml(manifest, pathToManifest)
  }

  // adds to <application> for Branch init:
  // <meta-data android:name="io.branch.sdk.BranchKey" android:value="key_live_icCccJIpd7GlYY5oOmoEtpafuDiuyXhT" />
  function updateBranchKeyMetaData (manifest, preferences) {
    var metadatas = manifest['manifest']['application'][0]['meta-data'] || []
    var androidName = 'io.branch.sdk.BranchKey'

    // remove old
    metadatas = removeBasedOnAndroidName(metadatas, androidName)

    // add new
    manifest['manifest']['application'][0]['meta-data'] = metadatas.concat([{
      '$': {
        'android:name': androidName,
        'android:value': preferences.branchKey
      }
    }])

    return manifest
  }

  // adds to <application> for install referrer tracking (optional)
  //    <receiver android:name="io.branch.referral.InstallListener" android:exported="true">
  //       <intent-filter>
  //           <action android:name="com.android.vending.INSTALL_REFERRER" />
  //       </intent-filter>
  //    </receiver>
  function updateBranchReferrerTracking (manifest) {
    var receivers = manifest['manifest']['application'][0]['receiver'] || []
    var androidName = 'io.branch.referral.InstallListener'

    // remove old
    receivers = removeBasedOnAndroidName(receivers, androidName)

    // add new
    manifest['manifest']['application'][0]['receiver'] = receivers.concat([{
      '$': {
        'android:name': androidName,
        'android:exported': true
      },
      'intent-filter': [{
        'action': [{
          '$': {
            'android:name': 'com.android.vending.INSTALL_REFERRER'
          }
        }]
      }]
    }])

    return manifest
  }

  function updateLaunchOptionToSingleTask (manifest, preferences) {
    manifest['manifest']['application'][0]['activity'][0]['$']['android:launchMode'] = 'singleTask'
  // adds to main <activity>:
  //    android:launchMode="singleTask"
    return manifest
  }

  function updateBranchURIScheme (manifest, preferences) {
    // TODO: need to validate main activity (second [0])
    var intentFilters = manifest['manifest']['application'][0]['activity'][0]['intent-filter'] || []

  // adds to main <activity> for URI Scheme
  //    <intent-filter>
  //        <data android:scheme="ethantest" android:host="open" />
  //        <action android:name="android.intent.action.VIEW" />
  //        <category android:name="android.intent.category.DEFAULT" />
  //        <category android:name="android.intent.category.BROWSABLE" />
  //    </intent-filter>
    // remove
    intentFilters = removeBasedOnIntentFilter(intentFilters)

    manifest['manifest']['application'][0]['activity'][0]['intent-filter'] = intentFilters.concat([{
    // add
      'action': [{
        '$': {
          'android:name': 'android.intent.action.VIEW'
        }
      }],
      'category': [{
        '$': {
          'android:name': 'android.intent.category.DEFAULT'
        }
      }, {
        '$': {
          'android:name': 'android.intent.category.BROWSABLE'
        }
      }],
      'data': [{
        '$': {
          'android:scheme': preferences.uriScheme,
          'android:host': 'open'
        }
      }]
    }])

    return manifest
  }

  function updateBranchAppLinks (manifest, preferences) {
    var intentFilters = manifest['manifest']['application'][0]['activity'][0]['intent-filter'] || []
  // adds to main <activity> for App Links (optional)
  //    <intent-filter android:autoVerify="true">
  //       <action android:name="android.intent.action.VIEW" />
  //       <category android:name="android.intent.category.DEFAULT" />
  //       <category android:name="android.intent.category.BROWSABLE" />
  //       <data android:scheme="https" android:host="ethan.app.link" />
  //       <data android:scheme="https" android:host="ethan-alternate.app.link" />
  //    </intent-filter>
    var data = getAppLinkIntentFilterData(preferences)

    manifest['manifest']['application'][0]['activity'][0]['intent-filter'] = intentFilters.concat([{
    // add new (remove old already done in updateBranchURIScheme)
      '$': {
        'android:autoVerify': 'true'
      },
      'action': [{
        '$': {
          'android:name': 'android.intent.action.VIEW'
        }
      }],
      'category': [{
        '$': {
          'android:name': 'android.intent.category.DEFAULT'
        }
      }, {
        '$': {
          'android:name': 'android.intent.category.BROWSABLE'
        }
      }],
      'data': data
    }])

    return manifest
  }

  // determine the Branch link domain <data> to append to the App Link intent filter
  function getAppLinkIntentFilterData (preferences) {
    var intentFilterData = []

    if (preferences.linkDomain.indexOf('app.link') !== -1) {
      // app.link needs an additional -alternate link domain
      var first = preferences.linkDomain.split('.')[0]
      var rest = preferences.linkDomain.split('.').slice(2).join('.')
      var alternate = first + '-alternate' + '.' + rest

      intentFilterData.push(getAppLinkIntentFilterDictionary(preferences.linkDomain))
      intentFilterData.push(getAppLinkIntentFilterDictionary(alternate))
    } else if (preferences.linkDomain.indexOf('bnc.lt') !== -1) {
      // bnc.lt
      if (preferences.androidPrefix == null) {
        throw new Error('Branch SDK plugin is missing "android-prefix" in <branch-config> in your config.xml')
      }

      intentFilterData.push(getAppLinkIntentFilterDictionary(preferences.linkDomain, preferences.androidPrefix))
    } else {
      // custom
      intentFilterData.push(getAppLinkIntentFilterDictionary(preferences.linkDomain))
    }

    return intentFilterData
  }

  // generate the array dictionary for <data> component for the App Link intent filter
  function getAppLinkIntentFilterDictionary (linkDomain, androidPrefix) {
    var scheme = 'https'
    var output = {
      '$': {
        'android:host': linkDomain,
        'android:scheme': scheme
      }
    }

    if (androidPrefix) {
      output['$']['android:pathPrefix'] = androidPrefix
    }

    return output
  }

  // remove previous Branch related Intent Filters (both URI Scheme and App Link)
  function removeBasedOnIntentFilter (items) {
    var without = []
    for (var i = 0; i < items.length; i++) {
      var item = items[i]
      if (item.hasOwnProperty('action') && item.hasOwnProperty('category') && item.hasOwnProperty('data')) {
        var actions = item['action']
        var categories = item['category']
        var data = item['data']

        if (actions.length === 1 && actions[0]['$'].hasOwnProperty('android:name') && actions[0]['$']['android:name'] === 'android.intent.action.VIEW' && categories.length === 2 && categories[0]['$'].hasOwnProperty('android:name') && (categories[0]['$']['android:name'] === 'android.intent.category.DEFAULT' || categories[0]['$']['android:name'] === 'android.intent.category.BROWSABLE') && categories[1]['$'].hasOwnProperty('android:name') && (categories[1]['$']['android:name'] === 'android.intent.category.DEFAULT' || categories[1]['$']['android:name'] === 'android.intent.category.BROWSABLE') && data.length > 0 && data.length < 3) {
          // URI Scheme
          if (data[0]['$'].hasOwnProperty('android:scheme') && data[0]['$'].hasOwnProperty('android:host') && data[0]['$']['android:host'] === 'open' && !item.hasOwnProperty('$')) {
            continue
          }

          // AppLink
          if (data[0]['$'].hasOwnProperty('android:host') && data[0]['$'].hasOwnProperty('android:scheme') && data[0]['$']['android:scheme'] === 'https' && item.hasOwnProperty('$') && item['$'].hasOwnProperty('android:autoVerify') && item['$']['android:autoVerify'] === 'true') {
            continue
          }
        }
      }
      without.push(item)
    }
    return without
  }

  // remove previous Branch related <meta-data> and <receiver> based on android:name
  function removeBasedOnAndroidName (items, androidName) {
    var without = []
    for (var i = 0; i < items.length; i++) {
      var item = items[i]
      if (item.hasOwnProperty('$') && item['$'].hasOwnProperty('android:name')) {
        var key = item['$']['android:name']
        if (key === androidName) {
          continue
        }
        without.push(item)
      }
    }
    return without
  }

  // function getMainLaunchActivityIndex (activities) {
  //   var launchActivityIndex = -1
  // get the main <activity> because Branch Intent Filters must be in the main Launch Activity

  //   activities.some(function (activity, index) {
  //     if (isLaunchActivity(activity)) {
  //       launchActivityIndex = index
  //       return true
  //     }

  //     return false
  //   })

  //   return launchActivityIndex
  // }

  // function isLaunchActivity (activity) {
  //   var intentFilters = activity['intent-filter']
  //   var isLauncher = false

  //   if (intentFilters == null || intentFilters.length === 0) {
  //     return false
  //   }

  //   isLauncher = intentFilters.some(function (intentFilter) {
  //     var action = intentFilter['action']
  //     var category = intentFilter['category']

  //     if (action == null || action.length !== 1 || category == null || category.length !== 1) {
  //       return false
  //     }

  //     var isMainAction = action[0]['$']['android:name'] === 'android.intent.action.MAIN'
  //     var isLauncherCategory = category[0]['$']['android:name'] === 'android.intent.category.LAUNCHER'

  //     return isMainAction && isLauncherCategory
  //   })

  //   return isLauncher
  // }
})()
