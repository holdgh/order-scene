@echo off
chcp 65001
echo 一次性创建项目所需的Kafka主题、分区、副本...
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.17+10
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "C:\englishprogram\kafka_2.13-4.1.0\bin\windows"
kafka-topics.bat --create ^
--topic track-topic ^
--bootstrap-server 127.0.0.1:9092 ^
--partitions 3 ^
--replication-factor 1
echo ✅ kafka配置成功！
pause
