package de.yotron.aws;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class awsProcessor {    private AmazonIdentityManagement iamClient;
    private awsConfiguration awsConfiguration = new awsConfiguration();

    public awsProcessor(String awsCredGroupName, String awsRegion) {
        this.iamClient = getAmazonIdentityManagement(awsCredGroupName, awsRegion);
    }

    private AmazonIdentityManagement getAmazonIdentityManagement(String awsCredGroupName, String awsRegion) {
        try {
            ProfileCredentialsProvider profCredProvider = new ProfileCredentialsProvider(awsCredGroupName);
            return AmazonIdentityManagementClientBuilder
                    .standard()
                    .withCredentials(profCredProvider)
                    .withRegion(awsRegion)
                    .withClientConfiguration(awsConfiguration.getClientConfiguration())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    public Boolean deleteAWSAccessKey(String accessKeyId, String awsUserName) {
        try {
            DeleteAccessKeyRequest requestDeleteAccessKey = new DeleteAccessKeyRequest()
                    .withAccessKeyId(accessKeyId)
                    .withUserName(awsUserName);
            DeleteAccessKeyResult resultDeleteAccessKey = this.iamClient.deleteAccessKey(requestDeleteAccessKey);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getAccessKeyId(String awsUserName) {
        try {
            ListAccessKeysRequest requestAccessKeyList = new ListAccessKeysRequest().withUserName(awsUserName);
            ListAccessKeysResult responseAccessKeyList = this.iamClient.listAccessKeys(requestAccessKeyList);
            if (responseAccessKeyList.isTruncated())
                throw new Exception("No AccessKeyList for User " + awsUserName + " in AWS found. Please Check Username and Credentials in AWSManagementConsole.");
            List<AccessKeyMetadata> responseAccessKeys = responseAccessKeyList.getAccessKeyMetadata();
            Optional<AccessKeyMetadata> accessKey = responseAccessKeys
                    .stream()
                    .filter(a -> a.getUserName().equals(awsUserName))
                    .filter(b -> b.getStatus().equals("Active"))
                    .findFirst();
            if (!accessKey.isPresent())
                throw new Exception("No active AccessKey for User " + awsUserName + " in AWS found. Please Check Username and Credentials in AWSManagementConsole.");
            return accessKey.get().getAccessKeyId();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    public UpdateAccessKeyResult setOldAccessKeyInactive(String awsUserName, String oldAccessKeyValue) {
        UpdateAccessKeyRequest requestUpdateAccessKey = new UpdateAccessKeyRequest(awsUserName, oldAccessKeyValue, StatusType.Inactive);
        return this.iamClient.updateAccessKey(requestUpdateAccessKey);
    }

    public LinkedHashMap<String, String> createNewAccessKey(String awsUserName) {
        try {
            CreateAccessKeyRequest requestCreate = new CreateAccessKeyRequest()
                    .withUserName(awsUserName);
            CreateAccessKeyResult createAccessKeyResult = this.iamClient.createAccessKey(requestCreate);
            LinkedHashMap<String, String> keyValues = new <String, String>LinkedHashMap();
            keyValues.put(awsConfiguration.getAccessKeyIdValue(), createAccessKeyResult.getAccessKey().getAccessKeyId());
            keyValues.put(awsConfiguration.getSecretKeyIdValue(), createAccessKeyResult.getAccessKey().getSecretAccessKey());
            return keyValues;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }
}
