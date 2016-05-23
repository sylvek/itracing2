node {
  withCredentials([[$class: 'FileBinding', credentialsId: 'KEYSTORE_PATH', variable: 'KEYSTORE_PATH'], [$class: 'StringBinding', credentialsId: 'KEYSTORE_PASSWORD', variable: 'KEYSTORE_PASSWORD']]) {
     sh 'fastlane android deploy keystore:${KEYSTORE_PATH} password:${KEYSTORE_PASSWORD}'
     archive '**/itracing2.apk'
  }
}
