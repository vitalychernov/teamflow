@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "__MVNW_ARG0_NAME__=%~nx0")
@SET ___MVNW_OUTCUR_STDOUT=1
@IF "%MVNW_VERBOSE%"=="" (
  @SET MVNW_VERBOSE=false
)

@SET "JAVA_HOME_PARENT=%JAVA_HOME%"
@IF "%JAVA_HOME_PARENT%"=="" (
  @FOR /F "usebackq tokens=2*" %%a IN (`REG QUERY "HKLM\SOFTWARE\JavaSoft\JDK" /v CurrentVersion 2^>nul`) DO (
    @SET "JAVA_CURRENT_VERSION=%%b"
  )
  @IF NOT "%JAVA_CURRENT_VERSION%"=="" (
    @FOR /F "usebackq tokens=2*" %%a IN (`REG QUERY "HKLM\SOFTWARE\JavaSoft\JDK\%JAVA_CURRENT_VERSION%" /v JavaHome 2^>nul`) DO (
      @SET "JAVA_HOME_PARENT=%%b"
    )
  )
)

@SET "JAVA_HOME=%JAVA_HOME_PARENT%"
@IF "%JAVA_HOME%"=="" (
  @ECHO Error: JAVA_HOME not set and java not found in PATH. 1>&2
  @EXIT /B 1
)

@SET "JAVA_EXECUTABLE=%JAVA_HOME%\bin\java.exe"
@IF NOT EXIST "%JAVA_EXECUTABLE%" (
  @ECHO Error: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
  @EXIT /B 1
)

@SET "MAVEN_PROJECTBASEDIR=%~dp0"

@IF "%MVNW_REPOURL%"=="" SET "MVNW_REPOURL=https://repo.maven.apache.org/maven2"
@SET "WRAPPER_JAR=%USERPROFILE%\.m2\wrapper\dists\maven-wrapper.jar"
@SET "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"
@SET "WRAPPER_URL=%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@IF NOT EXIST "%WRAPPER_JAR%" (
  @ECHO Downloading Maven Wrapper...
  @powershell -Command "&{[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'}" 2>nul
)

@"%JAVA_EXECUTABLE%" ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  "%WRAPPER_LAUNCHER%" ^
  "-Dmaven.home=%MAVEN_USER_HOME%" ^
  %*
