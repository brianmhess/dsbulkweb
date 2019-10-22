package hessian.dsbulkweb;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DsbulkwebConfig {
    private Map<String,String> options = new HashMap<String,String>();
    private static final String NULL_VALUE = "<NULL>";
    private static DsbulkwebConfig instance = new DsbulkwebConfig();
    private MultipartFile origFile = null;
    private String originalFilename = null;
    private List<Path> savedFiles = new ArrayList<Path>();

    public DsbulkwebConfig() {
        putConnector();
        putConnectorCsv();
        putConnectorJson();
        putSchema();
        putCodec();
        putDriver();
        putEngine();
        putExecutor();
        putLog();
    }

    public static DsbulkwebConfig defaultInstance() {
        return instance;
    }

    public String cmdOption(String optionKey) {
        String cmdOption = "--" + optionKey;
        cmdOption = cmdOption.replace("_", ".");
        return cmdOption;
    }

    public List<String> optionsList() {
        return addOptions(new ArrayList<String>());
    }

    public List<String> optionsList(String opt) {
        String[] array = new String[1];
        array[0] = opt;
        return optionsList(array);
    }
    public List<String> optionsList(String[] opts) {
        return addOptions(new ArrayList<String>(Arrays.asList(opts)));
    }

    public List<String> addOptions(List<String> optionsList) {
        for (Map.Entry<String,String> entry : options.entrySet()) {
            if (!entry.getValue().equals(NULL_VALUE)) {
                optionsList.add(cmdOption(entry.getKey()));
                optionsList.add(entry.getValue());
            }
        }
        return optionsList;
    }

    // upload: true for upload, false for URL/link
    public String simpleLoadFormCsv(boolean upload) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <form action=\"/loadit/" + ((upload)? "loaditUpload" : "loaditLink") + "\" method=\"post\" enctype=\"multipart/form-data\">\n");
        sb.append("  <input type=\"hidden\" id=\"connector_name\" name=\"connector_name\" value=\"csv\">");

        sb.append("  <table>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Instructions</legend>\n");
        sb.append("      <table width=\"100%\">\n");
        sb.append("        <tr><td><font color=\"blue\">Blue</font> fields are required</td></tr>\n");
        sb.append("        <tr><td>Fill out these fields just as you would to dsbulk</td></tr>\n");
        sb.append("      </table>\n");
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>CSV Options</legend>\n");
        sb.append(connectorCsvForm());
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Schema Options</legend>\n");
        sb.append(schemaForm());
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Executor Options</legend>\n");
        sb.append(executorForm());
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Logging Options</legend>\n");
        sb.append(logForm());
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Driver Options</legend>\n");
        sb.append(driverForm());
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Files</legend>\n");
        sb.append(filesForm(upload));
        sb.append("    </fieldset></td></tr>\n");

        sb.append("  </table>\n");
        sb.append("    <div class=\"button\"><button type=\"submit\">Load It!</button></div>\n");
        sb.append("  </form>");

        return sb.toString();
    }

    // upload: true for upload, false for URL/link
    public String simpleLoadFormJson(boolean upload) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <form action=\"/loadit/" + ((upload)? "loaditUpload" : "loaditLink") + "\" method=\"post\" enctype=\"multipart/form-data\">\n");
        sb.append("  <input type=\"hidden\" id=\"connector_name\" name=\"connector_name\" value=\"json\">");

        sb.append("  <table>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Instructions</legend>\n");
        sb.append("      <table width=\"100%\">\n");
        sb.append("        <tr><td><font color=\"blue\">Blue</font> fields are required</td></tr>\n");
        sb.append("        <tr><td>Fill out these fields just as you would to dsbulk</td></tr>\n");
        sb.append("      </table>\n");
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>JSON Options</legend>\n");
        sb.append(connectorJsonForm());
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Schema Options</legend>\n");
        sb.append(schemaForm());
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Executor Options</legend>\n");
        sb.append(executorForm());
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Logging Options</legend>\n");
        sb.append(logForm());
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Driver Options</legend>\n");
        sb.append(driverForm());
        sb.append("    </fieldset></td></tr>\n");

        sb.append("    <tr><td><fieldset>\n");
        sb.append("      <legend>Files</legend>\n");
        sb.append(filesForm(upload));
        sb.append("    </fieldset></td></tr>\n");

        sb.append("  </table>\n");
        sb.append("    <div class=\"button\"><button type=\"submit\">Load It!</button></div>\n");
        sb.append("  </form>");

        return sb.toString();
    }



    private void putConnector() {
        options.put("connector_name", "csv");
    }

    private String formEntry(String name, String text) {
        return formEntry(name, text, options.get(name));
    }

    private String formEntry(String name, String text, String value) {
        return  "        <tr>\n" +
                "          <td><label for=\"" + name + "\">" + text + "</label></td>\n" +
                "          <td><input type=\"text\" id=\"" + name + "\" name=\"" + name + "\" value=\"" + value + "\"></td>\n" +
                "        </tr>\n";
    }

    private String formEntryEmpty(String name, String text, boolean required) {
        return  "        <tr>\n" +
                "          <td><label for=\"" + name + "\"><font color=\"blue\">" + text + "</font></label></td>\n" +
                "          <td><input type=\"text\" id=\"" + name + "\" name=\"" + name + "\"" + (required ? " required" : "") + "></td>\n" +
                "        </tr>\n";
    }

    // upload: true for upload, false for URL/link
    private String filesForm(boolean upload) {
        String type = "text";
        String name = "connector_" + getConnector_name() + "_url";
        String prompt = "URL to File to Load";
        if (upload) {
            type = "file";
            name = "connector_" + getConnector_name() + "_file";
            prompt = "Local File to Load";
        }
        return  "      <table width=\"100%\">\n" +
                "        <tr>\n" +
                "          <td><label for=\"connector_csv_url\"><font color=\"blue\">" + prompt + ":</font></label></td>" +
                "          <td><input type=\"" + type + "\" id=\"" + name + "\" name=\"" + name + "\" required></td>" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td><label for=\"driver_cloud_secureConnectBundle\"><font color=\"blue\">Cloud Secure Connect Bundle:</font></label></td>" +
                "          <td><input type=\"file\" id=\"driver_cloud_secureConnectBundle\" name=\"driver_cloud_secureConnectBundle\" required></td>" +
                "        </tr>\n" +
                "      </table>\n";
    }

    private void putConnectorCsv() {
        options.put("connector_csv_url", NULL_VALUE);
        options.put("connector_csv_delimiter", ",");
        options.put("connector_csv_header", "true");
        options.put("connector_csv_skipRecords", "0");
        options.put("connector_csv_maxRecords", "-1");
        options.put("connector_csv_maxCharsPerColumn", "4096");
        options.put("connector_csv_newline", "auto");
        options.put("connector_csv_nullValue", NULL_VALUE);
    }

    private String connectorCsvForm() {
        return "      <table width=\"100%\">\n" +
                formEntry("connector_csv_delimiter", "CSV Delimiter") +
                formEntry("connector_csv_header", "Header (true/false)") +
                formEntry("connector_csv_skipRecords", "Recods to skip") +
                formEntry("connector_csv_maxRecords", "Max records to load") +
                formEntry("connector_csv_maxCharsPerColumn", "Max Chars/column") +
                formEntry("connector_csv_nullValue", "Null value") +
                "      </table>\n";
    }
    
    private void putConnectorJson() {
        options.put("connector_json_url", NULL_VALUE);
        options.put("connector_json_skipRecords", "0");
        options.put("connector_json_maxRecords", "-1");
        options.put("connector_json_mode", "MULTI_DOCUMENT");
    }

    private String connectorJsonForm() {
        return "      <table width=\"100%\">\n" +
                formEntry("connector_json_skipRecords", "Records to skipo") +
                formEntry("connector_json_maxRecords", "Max records to load") +
                formEntry("connector_json_mode", "JSON mode") +
                "      </table>\n";
    }

    
    private void putSchema() {
        options.put("schema_keyspace", NULL_VALUE);
        options.put("schema_table", NULL_VALUE);
        options.put("schema_mapping", NULL_VALUE);
        options.put("schema_allowExtraFields", "true");
        options.put("schema_allowMissingFields", "false");
        options.put("schema_nullToUnset", "true");
        options.put("schema_query", NULL_VALUE);
        options.put("schema_queryTimestamp", NULL_VALUE);
        options.put("schema_queryTtl", "-1");
    }

    private String schemaForm() {
        return "      <table width=\"100%\">\n" +
                formEntryEmpty("schema_keyspace", "Keyspace", true) +
                formEntryEmpty("schema_table", "Table", true) +
                formEntry("schema_mapping", "Mapping") +
                formEntry("schema_allowExtraFields", "Allow extra fields") +
                formEntry("schema_allowMissingFields", "Allow missing fields") +
                formEntry("schema_nullToUnset", "Nulls to unset") +
                formEntry("schema_query", "Query") +
                formEntry("schema_queryTimestamp", "Query timestamp") +
                formEntry("schema_queryTtl", "Query TTL") +
                "      </table>\n";
    }

    private void putCodec() {
        options.put("codec_booleanNumbers", "[1,0]");
        options.put("codec_booleanStrings", "[\"1:0\",\"Y:N\",\"T:F\",\"YES:NO\",\"TRUE:FALSE\"]");
        options.put("codec_date", "ISO_LOCAL_DATE");
        options.put("codec_locale", "en_US");
        options.put("codec_nullStrings", NULL_VALUE);
        options.put("codec_time", "ISO_LOCAL_TIME");
        options.put("codec_timeZone", "UTC");
        options.put("codec_timestamp", "CQL_TIMESTAMP");
        options.put("codec_unit", "MILLISECONDS");
        options.put("codec_uuidStrategy", "RANDOM");
    }

    private String codecForm() {
        return "      <table width=\"100%\">\n" +
                formEntry("codec_date", "Date format") +
                formEntry("codec_time", "Time format") +
                formEntry("codec_nullStrings", "Null strings") +
                "      </table>\n";
    }

    private void putDriver() {
        options.put("driver_cloud_secureConnectBundle", NULL_VALUE);
        options.put("driver_policy_maxRetries", "10");
        options.put("driver_query_consistency", "LOCAL_QUORUM");
        options.put("driver_query_idempotence", "true");
        options.put("driver_query_serialConsistency", "LOCAL_SERIAL");
        options.put("driver_auth_username", NULL_VALUE);
        options.put("driver_auth_password", NULL_VALUE);
    }

    private String driverForm() {
        return "      <table width=\"100%\">\n" +
                formEntryEmpty("driver_auth_username", "Username", true) +
                //formEntryEmpty("driver_auth_password", "Password", true) +
                "        <tr>\n" +
                "          <td><label for=\"driver_auth_password\"><font color=\"blue\">Password</font></label></td>\n" +
                "          <td><input type=\"password\" id=\"driver_auth_password\" name=\"driver_auth_password\" required></td>\n" +
                "        </tr>\n" +
                formEntry("driver_query_consistency", "Consistency Level") +
                //formEntry("driver_query_idempotence", "Query idempotence") +
                //formEntry("driver_query_serialConsistency", "Serial consistency level") +
                "      </table>\n";
    }

    private void putEngine() {
        options.put("engine_executionId", NULL_VALUE);
    }

    private String engineForm() {
        return "";
    }

    private void putExecutor() {
        options.put("executor_maxPerSecond", "-1");
        options.put("executor_maxInFlight", "1024");
    }

    private String executorForm() {
        return "      <table width=\"100%\">\n" +
                formEntry("executor_maxPerSecond", "Max records per second") +
                formEntry("executor_maxInFlight", "Max records in flight") +
                "      </table>\n";
    }

    private void putLog() {
        options.put("log_maxErrors", "100");
        options.put("log_directory", NULL_VALUE);
        options.put("log_verbosity", "1");
    }

    private String logForm() {
        return "      <table width=\"100%\">\n" +
                formEntry("log_maxErrors", "Max number of errors") +
                "      </table>\n";
    }



    public String getConnector_name() {
        return options.get("connector_name");
    }

    public void setConnector_name(String val) {
        options.put("connector_name", val);
    }

    public String getConnector_csv_file() {
        return options.get("connector_csv_url");
    }

    public void setConnector_csv_file(MultipartFile val) {
        origFile = val;
        originalFilename = val.getOriginalFilename();
        options.put("connector_csv_url", saveTmpFile(val, "file-", ".csv"));
    }

    public String getConnector_csv_url() {
        return options.get("connector_csv_url");
    }

    public void setConnector_csv_url(String val) {
        originalFilename = val;
        options.put("connector_csv_url", val);
    }

    public String getConnector_csv_delimiter() {
        return options.get("connector_csv_delimiter");
    }

    public void setConnector_csv_delimiter(String val) {
        options.put("connector_csv_delimiter", val);
    }

    public String getConnector_csv_header() {
        return options.get("connector_csv_header");
    }

    public void setConnector_csv_header(String val) {
        options.put("connector_csv_header", val);
    }

    public String getConnector_csv_skipRecords() {
        return options.get("connector_csv_skipRecords");
    }

    public void setConnector_csv_skipRecords(String val) {
        options.put("connector_csv_skipRecords", val);
    }

    public String getConnector_csv_maxRecords() {
        return options.get("connector_csv_maxRecords");
    }

    public void setConnector_csv_maxRecords(String val) {
        options.put("connector_csv_maxRecords", val);
    }

    public String getConnector_csv_maxCharsPerColumn() {
        return options.get("connector_csv_maxCharsPerColumn");
    }

    public void setConnector_csv_maxCharsPerColumn(String val) {
        options.put("connector_csv_maxCharsPerColumn", val);
    }

    public String getConnector_csv_newline() {
        return options.get("connector_csv_newline");
    }

    public void setConnector_csv_newline(String val) {
        options.put("connector_csv_newline", val);
    }

    public String getConnector_csv_nullValue() {
        return options.get("connector_csv_nullValue");
    }

    public void setConnector_csv_nullValue(String val) {
        options.put("connector_csv_nullValue", val);
    }

    public String getConnector_json_file() {
        return options.get("connector_json_url");
    }

    public void setConnector_json_file(MultipartFile val) {
        origFile = val;
        originalFilename = val.getOriginalFilename();
        options.put("connector_json_url", saveTmpFile(val, "file-", ".json"));
    }

    public String getConnector_json_url() {
        return options.get("connector_json_url");
    }

    public void setConnector_json_url(String val) {
        originalFilename = val;
        options.put("connector_json_url", val);
    }

    public String getConnector_json_skipRecords() {
        return options.get("connector_json_skipRecords");
    }

    public void setConnector_json_skipRecords(String val) {
        options.put("connector_json_skipRecords", val);
    }

    public String getConnector_json_maxRecords() {
        return options.get("connector_json_maxRecords");
    }

    public void setConnector_json_maxRecords(String val) {
        options.put("connector_json_maxRecords", val);
    }

    public String getConnector_json_mode() {
        return options.get("connector_json_mode");
    }

    public void setConnector_json_mode(String val) {
        options.put("connector_json_mode", val);
    }

    public String getSchema_keyspace() {
        return options.get("schema_keyspace");
    }

    public void setSchema_keyspace(String val) {
        options.put("schema_keyspace", val);
    }

    public String getSchema_table() {
        return options.get("schema_table");
    }

    public void setSchema_table(String val) {
        options.put("schema_table", val);
    }

    public String getSchema_mapping() {
        return options.get("schema_mapping");
    }

    public void setSchema_mapping(String val) {
        options.put("schema_mapping", val);
    }

    public String getSchema_allowExtraFields() {
        return options.get("schema_allowExtraFields");
    }

    public void setSchema_allowExtraFields(String val) {
        options.put("schema_allowExtraFields", val);
    }

    public String getSchema_allowMissingFields() {
        return options.get("schema_allowMissingFields");
    }

    public void setSchema_allowMissingFields(String val) {
        options.put("schema_allowMissingFields", val);
    }

    public String getSchema_nullToUnset() {
        return options.get("schema_nullToUnset");
    }

    public void setSchema_nullToUnset(String val) {
        options.put("schema_nullToUnset", val);
    }

    public String getSchema_query() {
        return options.get("schema_query");
    }

    public void setSchema_query(String val) {
        options.put("schema_query", val);
    }

    public String getSchema_queryTimestamp() {
        return options.get("schema_queryTimestamp");
    }

    public void setSchema_queryTimestamp(String val) {
        options.put("schema_queryTimestamp", val);
    }

    public String getSchema_queryTtl() {
        return options.get("schema_queryTtl");
    }

    public void setSchema_queryTtl(String val) {
        options.put("schema_queryTtl", val);
    }

    public String getCodec_booleanNumbers() {
        return options.get("codec_booleanNumbers");
    }

    public void setCodec_booleanNumbers(String val) {
        options.put("codec_booleanNumbers", val);
    }

    public String getCodec_booleanStrings() {
        return options.get("codec_booleanStrings");
    }

    public void setCodec_booleanStrings(String val) {
        options.put("codec_booleanStrings", val);
    }

    public String getCodec_date() {
        return options.get("codec_date");
    }

    public void setCodec_date(String val) {
        options.put("codec_date", val);
    }

    public String getCodec_locale() {
        return options.get("codec_locale");
    }

    public void setCodec_locale(String val) {
        options.put("codec_locale", val);
    }

    public String getCodec_nullStrings() {
        return options.get("codec_nullStrings");
    }

    public void setCodec_nullStrings(String val) {
        options.put("codec_nullStrings", val);
    }

    public String getCodec_time() {
        return options.get("codec_time");
    }

    public void setCodec_time(String val) {
        options.put("codec_time", val);
    }

    public String getCodec_timeZone() {
        return options.get("codec_timeZone");
    }

    public void setCodec_timeZone(String val) {
        options.put("codec_timeZone", val);
    }

    public String getCodec_timestamp() {
        return options.get("codec_timestamp");
    }

    public void setCodec_timestamp(String val) {
        options.put("codec_timestamp", val);
    }

    public String getCodec_unit() {
        return options.get("codec_unit");
    }

    public void setCodec_unit(String val) {
        options.put("codec_unit", val);
    }

    public String getCodec_uuidStrategy() {
        return options.get("codec_uuidStrategy");
    }

    public void setCodec_uuidStrategy(String val) {
        options.put("codec_uuidStrategy", val);
    }

    public String getDriver_cloud_secureConnectBundle() {
        return options.get("driver_cloud_secureConnectBundle");
    }

    public void setDriver_cloud_secureConnectBundle(MultipartFile val) {
        options.put("driver_cloud_secureConnectBundle", saveTmpFile(val, "dsescb-", ".zip"));
    }

    public String getDriver_auth_username() {
        return options.get("driver_auth_username");
    }

    public void setDriver_auth_username(String val) {
        options.put("driver_auth_username", val);
    }

    public String getDriver_auth_password() {
        return options.get("driver_auth_password");
    }

    public void setDriver_auth_password(String val) {
        options.put("driver_auth_password", val);
    }

    public String getDriver_policy_maxRetries() {
        return options.get("driver_policy_maxRetries");
    }

    public void setDriver_policy_maxRetries(String val) {
        options.put("driver_policy_maxRetries", val);
    }

    public String getDriver_query_consistency() {
        return options.get("driver_query_consistency");
    }

    public void setDriver_query_consistency(String val) {
        options.put("driver_query_consistency", val);
    }

    public String getDriver_query_idempotence() {
        return options.get("driver_query_idempotence");
    }

    public void setDriver_query_idempotence(String val) {
        options.put("driver_query_idempotence", val);
    }

    public String getDriver_query_serialConsistency() {
        return options.get("driver_query_serialConsistency");
    }

    public void setDriver_query_serialConsistency(String val) {
        options.put("driver_query_serialConsistency", val);
    }

    public String getEngine_executionId() {
        return options.get("engine_executionId");
    }

    public void setEngine_executionId(String val) {
        options.put("engine_executionId", val);
    }

    public String getExecutor_maxPerSecond() {
        return options.get("executor_maxPerSecond");
    }

    public void setExecutor_maxPerSecond(String val) {
        options.put("executor_maxPerSecond", val);
    }

    public String getExecutor_maxInFlight() {
        return options.get("executor_maxInFlight");
    }

    public void setExecutor_maxInFlight(String val) {
        options.put("executor_maxInFlight", val);
    }

    public String getLog_maxErrors() {
        return options.get("log_maxErrors");
    }

    public void setLog_maxErrors(String val) {
        options.put("log_maxErrors", val);
    }

    public String getLog_directory() {
        return options.get("log_directory");
    }

    public void setLog_directory(String val) {
        options.put("log_directory", val);
    }

    public String getLog_verbosity() {
        return options.get("log_verbosity");
    }

    public void setLog_verbosity(String val) {
        options.put("log_verbosity", val);
    }

    public MultipartFile getOrigFile() {
        return this.origFile;
    }

    public String getOriginalFilename() {
        return this.originalFilename;
    }

    public static String pathToString(Path path) {
        if (path.getFileSystem().getSeparator().equals("\\"))
            return path.toString().replace("\\", "\\\\");
        return path.toString();

    }

    private String saveTmpFile(MultipartFile val, String prefix, String suffix) {
        try {
            Path tmpPath = Files.createTempFile(prefix, suffix);
            FileOutputStream fileOutputStream = new FileOutputStream(tmpPath.toFile());
            fileOutputStream.write(val.getBytes());
            fileOutputStream.close();
            savedFiles.add(tmpPath);
            System.err.println("Saved tmpfile: " + tmpPath.toString());
            return pathToString(tmpPath);
        }
        catch (IOException ioe) {
            throw new RuntimeException("Could not save multipart file (" + val.getOriginalFilename() + ")");
        }
    }

    public boolean deleteTmpFiles() {
        boolean allOk = true;
        for (Path p : savedFiles) {
            System.err.println("Deleting tmpfile: " + p.toString());
            boolean deleted = p.toFile().delete();
            if (!deleted) {
                System.err.println("Could not delete file (" + p.toString() + ")");
                p.toFile().deleteOnExit();
            }
            allOk = allOk && deleted;
        }
        return allOk;
    }

}
