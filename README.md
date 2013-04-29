CS283
=====

<pre>
NOTE: Any of the links and credentials pointed to by the following sample URLs may become inactive at
any time. Please replace them with your own.

The application is setup to allow for reconfiguration without recompiling. The only hardcoded values
exist in ClientProperties.java and ServerProperties.java and they represent the URL at which the
actual property files can be found. 

sample client.properties
-------------------------------------------------------------------------------
SERVER_HEAD_HOST = ec2-107-22-42-96.compute-1.amazonaws.com
SERVER_HEAD_PORT = 5000
SERVER_WORKER_PORT = 5000
DIRECTORY_MONITOR_DELAY_TIME = 500
-------------------------------------------------------------------------------

sample server.properties
-------------------------------------------------------------------------------
AWS_ACCESS_KEY = AKIAINPCBUE4GPZJX2WQ
AWS_SECRET_KEY = hgGlikrZR5cdoOSZanrrQxUkjmERE6/I7Y0PyVaa
AWS_ENDPOINT = ec2.us-east-1.amazonaws.com
    
AWS_RUN_INSTANCE_TYPE = t1.micro
AWS_RUN_IMAGE_ID = ami-3fec7956
AWS_RUN_SECURITY_GROUP_ID = sg-35a9575e
AWS_RUN_KEY_NAME = FileSync

LOCAL_SCRIPT_URL = https://dl.dropboxusercontent.com/s/tydprhnx2lw364g/local.sh?token_hash=AAGG5Nf9FRAjoVBOka0X49ykkob6zds_3PT04YVZ1OpVQQ&dl=1
SETUP_SCRIPT_URL = https://dl.dropboxusercontent.com/s/bdtb15mp5zohqle/setup.sh?token_hash=AAFTuvLwYevA-DHvH77RdhicbMZ1CxMc9NdJLruI9ucVpg&dl=1
FILE_SYNC_PEM_URL = https://dl.dropboxusercontent.com/s/6c1t9i832vwffne/FileSync.pem?token_hash=AAFe9UWW80N9gYXOJ04yhyjW5rbxWv2Hz-6vH_A2v6eprQ&dl=1

LOCAL_SCRIPT_EXECUTE = /home/ubuntu/local.sh

HEAD_PORT = 5000
-------------------------------------------------------------------------------
Note: The security group must allow incoming traffic at ports 22, 80, and 5000.
Note: Key name must be named FileSync.pem

local.sh
-------------------------------------------------------------------------------
#!/bin/bash
chmod 400 FileSync.pem
ssh -i FileSync.pem -o "StrictHostKeyChecking no" ubuntu@$1 < /home/ubuntu/setup.sh

setup.sh
-------------------------------------------------------------------------------
#!/bin/bash
# clean up
rm -rf *jar *sh
# start downloading the jar file
wget --output-document=FileSyncServer.jar --quiet https://dl.dropboxusercontent.com/s/jadc3i9afisduqz/FileSyncServer.jar?token_hash=AAGmUsbeSx3FKNG3JCcvijm2Qtnd81nsJgrW4-8t3o8ong&dl=1
# if we don't wait a bit then the update and/or install will fail
sleep 10
sudo apt-get update > update.txt
sudo apt-get install -y openjdk-7-jre-headless apache2 > install.txt
sudo chown -R ubuntu /var/www
chmod +x FileSyncServer.jar
echo "#!/bin/bash" > startServer.sh
echo "java -jar FileSyncServer.jar 5000 /var/www" >> startServer.sh
at now -f startServer.sh
sleep 5
-------------------------------------------------------------------------------
</pre>