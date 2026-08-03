@echo off
@setlocal enabledelayedexpansion

set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9-bin

if not exist "%MAVEN_HOME%" (
    echo Downloading Maven...
    mkdir "%MAVEN_HOME%" 2>nul
    powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip' -OutFile '%TEMP%\maven.zip'"
    powershell -Command "Expand-Archive '%TEMP%\maven.zip' -DestinationPath '%MAVEN_HOME%' -Force"
    del "%TEMP%\maven.zip" 2>nul
)

set WRAPPER_JAR=%~dp0\.mvn\wrapper\maven-wrapper.jar

if not exist "%WRAPPER_JAR%" (
    if not exist "%~dp0\.mvn\wrapper" mkdir "%~dp0\.mvn\wrapper"
    echo Downloading Maven Wrapper...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar' -OutFile '%WRAPPER_JAR%'"
)

set MAVEN_CMD="%MAVEN_HOME%\apache-maven-3.9.9\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
    echo Maven not found at %MAVEN_CMD%
    echo Please check the installation.
    exit /b 1
)

%MAVEN_CMD% %*
