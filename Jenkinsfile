pipeline {
    agent {
        label 'master'
    }

    tools {
        maven 'Maven 3.6.0'
        jdk 'OpenJDK 11'
    }

    stages {
        stage('Run tests and Sonar analysis') {
            steps {
                configFileProvider(
                        [configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS clean verify sonar:sonar'
                }
            }
        }
        stage('Deploy artifact') {
            steps {
                configFileProvider(
                        [configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS clean deploy -Dmaven.test.skip=true'
                }
            }
        }
    }
}
