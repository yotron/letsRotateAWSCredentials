
# [ ** <span style="color:green">YO</span>TRON** ](https://www.yotron.de)

[ ** <span style="color:green">YO</span>TRON ** ](https://www.yotron.de) is a company which is focused on Big Data, 
Cloud Computing and Data Management with NOSQL and SQL-Databases. Visit us on [ www.yotron.de ](https://www.yotron.de)

# AWSAccessCredentialRotation
Secure standards recommend to change the access credentials frequently. Also in AWS it is recommend to rotate the credentials.
To access AWS from AWS Command Client Interface (AWS Cli) or AWS Application Interface (AWS API) the usage of "Access Keys" is the 
preferred way to communicate with AWS.

https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html

Currently AWS Access Keys must be created manually with the AWS Management Console or via Cloudformation. A recommended frequent change of the 
access credentials must be donne manually.
Goal of this tool is to process the change automatically. It allows to rotate the  "Access key ID" and the "AWS secret access key"  from 
a local system or a AWS system (e.g., EC2-instances).

The tools allows:
- creation new credentials (AWS access key ID, AWS secret access key)
- deletion of old credentials
- update of the credential file of the local system or EC2-instance (usually <HomeFolder>/.aws/credentials)
- provision of the new credential in JSON

The tool runs also in a proxy environment.

### technical requirements
- Java 8 or higher
- Maven
- AWS Account
- (recommended) AWS CLI

### requirements AWS credentials
Inside AWS a technical user with the approppriate AWS policies is needed. The policies are.
- iam:CreateAccessKey
- iam:DeleteAccessKey
- iam:UpdateAccessKey

### content of the project
| folder         | description                                           |
| -------------- | ----------------------------------------------------- |
| cloudformation | Example of a cloudformation script to generate the appropriate AWS credential. It is recommended to cerate an own script. |
| de.yotron.aws  | Java content of the project. |
| conf.setting   | Configuration file with the setting of the project (project, proxy) |

### configuration file
The project can be configured with the "conf.settings" file. The content of the file is.

#### location
The configuration file is located in the same directory as the rotatedAWSCredential.jar-file.

#### setting
| name       | group           | description                                           |
| ---------- | --------------- | ----------------------------------------------------- |
| accesskeyid | AWS | Name of the AccessKeyAttribute in AWS-CLI credential file. Usually "aws_access_key_id" |
| secretaccesskeyid | AWS | Name of the SecretKeyAttribute in AWS-CLI credential file. Usually "aws_secret_access_key" |
| credgroupname | AWS | The group name with the AccessCredentials in AWS-CLI credential file, "[logRotation]" = "logRotation". |
| username | AWS | Name of the AWS-User for whom the AccessKey has to be rotated.
| region | AWS | AWS-Region to run teh tool. |
| credentialsfilepath | AWS | Path of the credential file. Usually <HomeFolder>/.aws/credentials |
| host | proxy | Host of the proxy-server |
| port | proxy | port of the proxy-server |
| nonproxyhosts | proxy | List (comma-separated) of the host, where the proxy has to be ignored |
| username | proxy | username to authenticate the user against the proxy |
| password | proxy | password to authenticate the user against the proxy |

If the proxy setting is not needed, please uncomment the setting with "#".

### installation
This is a maven project. You can integrate the code into your own project or create the jar-artifacts by yourself.

### Run process
To run the jar just call

java -jar rotatedAWSCredentials.jar

### Run in code
To run the process in your code:

```
import de.yotron.aws.rotateAWSCredential;
...
rotateAWSCredential aws = new rotateAWSCredential();
String JSONResult = aws.rotateAccessKey());
```

### Result
The result is a json with

```
{
  "aws_access_key_id":"AK..............UQ",
  "aws_secret_access_key":"ply............................vHKy"
}
```
### own credentials
created by Joern Kleinbub, YOTRON, 30.05.2018

info@yotron.de, www.yotron.de