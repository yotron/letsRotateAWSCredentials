package de.yotron.aws;

import com.amazonaws.ClientConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class awsConfiguration {
    private ClientConfiguration clientConfiguration = new ClientConfiguration();
    private LinkedHashMap<String, LinkedHashMap> configuration = new <String, String>LinkedHashMap();

    public awsConfiguration() {
        File configFile=new File(awsConfiguration.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"\\..\\conf.settings");
        if (!configFile.isFile()) {
            configFile=new File(awsConfiguration.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"\\conf.settings");
        }
        this.configuration = getSetting(configFile);
    }

    public void setConfiguration(LinkedHashMap<String, LinkedHashMap> configuration) {
        this.configuration = configuration;
    }

    public ClientConfiguration getClientConfiguration () {
        if (this.getProxyValue("host") != null) this.clientConfiguration.setProxyHost(this.getProxyValue("host"));
        if (this.getProxyValue("port") != null) this.clientConfiguration.setProxyPort(Integer.parseInt(this.getProxyValue("port")));
        if (this.getProxyValue("nonproxyhosts") != null) this.clientConfiguration.setNonProxyHosts(this.getProxyValue("nonproxyhosts"));
        if (this.getProxyValue("username") != null) this.clientConfiguration.setProxyUsername(this.getProxyValue("username"));
        if (this.getProxyValue("password") != null) this.clientConfiguration.setProxyPassword(this.getProxyValue("password"));
        return this.clientConfiguration;
    }

    public String getValue(String group, String key) {
        return (String)(this.configuration.get(group)).get(key);
    }

    public String getProxyValue(String key) {
        try {
            return this.getValue("proxy", key);
        } catch (Exception e) {
            return null;
        }
    }

    public String getAccessKeyIdValue() {
        try {
            return this.getValue("aws", "accesskeyid");
        } catch (Exception e) {
            return null;
        }
    }

    public String getSecretKeyIdValue() {
        try {
            return this.getValue("aws", "secretaccesskeyid");
        } catch (Exception e) {
            return null;
        }
    }

    public String getUsername() {
        try {
            return this.getValue("aws", "username");
        } catch (Exception e) {
            return null;
        }
    }

    public String getGroupNameValue() {
        try {
            return this.getValue("aws", "credgroupname");
        } catch (Exception e) {
            return null;
        }
    }

    public String getRegionValue() {
        try {
            return this.getValue("aws", "region");
        } catch (Exception e) {
            return null;
        }
    }

    public String getAWSValue(String key) {
        try {
            return this.getValue("aws", key);
        } catch (Exception e) {
            return null;
        }
    }

    public static LinkedHashMap<String, LinkedHashMap> getSetting(File configFile){
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            LinkedHashMap<String, LinkedHashMap> configuration = new <String, String>LinkedHashMap();
            LinkedHashMap<String, String> keyValues = new <String, String>LinkedHashMap();
            String line, group = "";
            while ((line = br.readLine()) != null) {
                Matcher uncommentMatcher = Pattern.compile("\\s*#.*").matcher(line);
                Matcher groupMatcher = Pattern.compile("\\[.*.\\]").matcher(line);
                Matcher keyValueMatcher = Pattern.compile(".*=.*").matcher(line);
                if (!uncommentMatcher.matches()) {
                    if (groupMatcher.matches()) {
                        if (!keyValues.isEmpty()) {
                            configuration.put(group, new LinkedHashMap<String, String>(keyValues));
                        }
                        keyValues.clear();
                        group = line.replace("[", "").replace("]", "");
                    } else if (keyValueMatcher.matches()) {
                        String[] parts = line.split(" = ", 2);
                        if (parts.length == 2) {
                            keyValues.put(parts[0], parts[1]);
                        } else {
                            keyValues.put(parts[0], "");
                        }
                    }
                }
            }
            configuration.put(group, new LinkedHashMap<String, String>(keyValues));
            return configuration;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }
}
