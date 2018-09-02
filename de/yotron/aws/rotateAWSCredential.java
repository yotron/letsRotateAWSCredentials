package de.yotron.aws;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONObject;

public class rotateAWSCredential {
    private String awsCredGroupName;
    private String awsUserName;
    private String awsRegion;
    private String filePath;
    private awsProcessor awsProcessor;
    private awsConfiguration awsConfiguration = new awsConfiguration();
    private LinkedHashMap<String,String> newValues = new LinkedHashMap<String,String>();

    public rotateAWSCredential() {
        try {
            setVariable();
            File propFile = getPropFile();
            LinkedHashMap<String, LinkedHashMap> accessCredentialOld = awsConfiguration.getSetting(propFile);
            checkAccessCredentialOldConfiguration(accessCredentialOld);
            awsProcessor = new awsProcessor(this.awsCredGroupName,this.awsRegion);
            String oldAccessKeyId = awsProcessor.getAccessKeyId(this.awsUserName);
            initiateNewAccessKeyConfiguration(propFile, accessCredentialOld);
            clearOldAccessKeyConfiguration(propFile, oldAccessKeyId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String rotateAccessKey() {
        return new JSONObject(this.newValues).toString();
    }

    public static void main(String[] args) {
        rotateAWSCredential aws = new rotateAWSCredential();
        System.out.println(aws.rotateAccessKey());

    }

    private void setVariable() {
        awsCredGroupName = awsConfiguration.getAWSValue("credgroupname");
        awsRegion = awsConfiguration.getAWSValue("region");
        filePath = awsConfiguration.getAWSValue("credentialsfilepath");
        awsUserName = awsConfiguration.getAWSValue("username");
    }

    private Boolean clearOldAccessKeyConfiguration(File propFile, String oldAccessKeyId) {
        try {
            Boolean delAWSAccKey = awsProcessor.deleteAWSAccessKey(oldAccessKeyId, this.awsUserName);
            Boolean resultRemoveOldCredentialFile = removeCredentialFile(new File(propFile.toPath() + "_old"));
            if (delAWSAccKey == null || !resultRemoveOldCredentialFile)
                throw new Exception("An error during the clearance of the oldAccessKeys occured. Please clear the credential file and the AWS-Access Key manually.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    private Boolean initiateNewAccessKeyConfiguration(File propFile, LinkedHashMap<String, LinkedHashMap> accessCredentialOld) {
        try {
            LinkedHashMap<String, LinkedHashMap> accessCredentialNew = getNewAccessKeyCredentials(accessCredentialOld);
            Boolean resultWriteCredentialFile = writeNewCredentialFile(propFile, accessCredentialNew);
            if (!resultWriteCredentialFile) {
                Boolean revertResult = revertNewAccessKey(propFile, accessCredentialNew);
                if (!revertResult)
                    throw new Exception("An error during the revert of the new credentials occured. Please revert the change manually.");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return false;
        }
    }

    private Boolean revertNewAccessKey(File propFile, LinkedHashMap<String, LinkedHashMap> accessCredentialNew) {
        try {
            awsProcessor.deleteAWSAccessKey(((LinkedHashMap<String, String>) accessCredentialNew.get(awsCredGroupName)).get(awsConfiguration.getAccessKeyIdValue()),this.awsUserName);
            removeCredentialFile(propFile);
            Files.copy(new File(propFile.toPath() + "_old").toPath(), propFile.toPath());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Boolean removeCredentialFile(File propFile)  {
        try {
            Files.delete(propFile.toPath());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    private Boolean writeNewCredentialFile(File propFile, LinkedHashMap<String, LinkedHashMap> accessCredentialNew) {
        try {
            Boolean resultCopyFile = createOldCredentialFile(propFile);
            Boolean resultOverwriteProductiveFile = overwriteProductiveCredentialFile(propFile, accessCredentialNew);
            return resultCopyFile && resultOverwriteProductiveFile?true:false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Boolean overwriteProductiveCredentialFile(File propFile, LinkedHashMap<String, LinkedHashMap> accessCredentialNew) {
        try {
            String fileContent = "";
            for (String key : accessCredentialNew.keySet()) {
                fileContent = fileContent + "[" + key + "]\n";
                for (Object keyValue : accessCredentialNew.get(key).entrySet()) {
                    fileContent = fileContent + ((Map.Entry<String, String>) keyValue).getKey() + " = " + ((Map.Entry<String, String>) keyValue).getValue() + "\n";
                }
            }
            Files.write(propFile.toPath(), fileContent.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Boolean createOldCredentialFile(File propFile) throws IOException {
        try {
            Files.copy(propFile.toPath(), new File(propFile.toPath() + "_old").toPath());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Files.delete(new File(propFile.toPath() + "_old").toPath());
            return false;
        }
    }

    private LinkedHashMap<String, LinkedHashMap> getNewAccessKeyCredentials(LinkedHashMap<String, LinkedHashMap> accessCredentialOld) {
        LinkedHashMap<String, String> responseCreate = awsProcessor.createNewAccessKey(this.awsUserName);
        LinkedHashMap<String, LinkedHashMap> accessCredentialNew = getNewKeyAccessCredentials(accessCredentialOld, responseCreate);
        if (accessCredentialNew.isEmpty()) {
            awsProcessor.deleteAWSAccessKey(responseCreate.get(awsConfiguration.getAccessKeyIdValue()),this.awsUserName);
            System.exit(0);
        }
        newValues.put(awsConfiguration.getAccessKeyIdValue(), responseCreate.get(awsConfiguration.getAccessKeyIdValue()));
        newValues.put(awsConfiguration.getSecretKeyIdValue(), responseCreate.get(awsConfiguration.getSecretKeyIdValue()));
        return accessCredentialNew;
    }

    private LinkedHashMap<String, LinkedHashMap> getNewKeyAccessCredentials(LinkedHashMap<String, LinkedHashMap> accessCredentialOld, LinkedHashMap<String, String> keyValues) {
        try {
            LinkedHashMap<String, LinkedHashMap> accessCredentialNew = new <String, String>LinkedHashMap(accessCredentialOld);
            accessCredentialNew.put(awsCredGroupName, new LinkedHashMap<String, String>(keyValues));
            return accessCredentialNew;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void checkAccessCredentialOldConfiguration(LinkedHashMap<String, LinkedHashMap> accessCredentialOld) {
        try {
            if (!accessCredentialOld.containsKey(awsCredGroupName))
                throw new Exception("Group " + awsCredGroupName + " not found in CredentialFile");
            if (!accessCredentialOld.get(awsCredGroupName).containsKey(awsConfiguration.getAccessKeyIdValue()))
                throw new Exception("Key " + awsConfiguration.getAccessKeyIdValue() + " not found in CredentialFile");
            if (!accessCredentialOld.get(awsCredGroupName).containsKey(awsConfiguration.getSecretKeyIdValue()))
                throw new Exception("Key " + awsConfiguration.getSecretKeyIdValue() + " not found in CredentialFile");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private File getPropFile() {
        try {
            File file = new File(this.filePath);
            if (!file.exists()) throw new FileNotFoundException("AWSCredentialFile " + this.filePath + " not found!");
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }
}
