node {
  withCredentials([
    [$class: 'FileBinding', credentialsId: 'KEYSTORE_PATH', variable: 'KEYSTORE_PATH'],
    [$class: 'StringBinding', credentialsId: 'KEYSTORE_PASSWORD', variable: 'KEYSTORE_PASSWORD'],
    [$class: 'FileBinding', credentialsId: 'JSON_KEY_PATH', variable: 'SUPPLY_JSON_KEY']
    ]) {
     checkout scm
     stage 'action'

     stage 'build'
     sh 'fastlane android deploy keystore:${KEYSTORE_PATH} password:${KEYSTORE_PASSWORD}'

     stage 'archive'
     archive '**/itracing2.apk'
  }
}
