package hessian.dsbulkweb;

import com.datastax.oss.driver.shaded.guava.common.io.Files;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.datastax.dsbulk.engine.DataStaxBulkLoader;

import java.nio.file.Path;
import java.util.List;

@RestController
public class DsbulkwebController {
    @RequestMapping("/")
    public String index() {
        return index_with_message(null);
    }

    private String index_with_message(String message) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <body>\n" +
                "    <table>\n" +
                "      <tr>\n" +
                "        <td><image src=\"/vest.png\" title=\"Load It\" width=\"100\" height=\"100\"></image></td>\n" +
                "        <td><font size=\"7\">Welcome! Let's load some data!</font></td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "\n" +
                ((null != message) ? message : "") +
                "    <ul>\n" +
                "        <li><a href=\"/loadit/csvsimple_upload\">Load CSV by uploading file (simple)</a></li>\n" +
                "        <li><a href=\"/loadit/csvsimple_link\">Load CSV by URL to file (simple)</a></li>\n" +
                "\n" +
                "        <li><a href=\"/loadit/jsonsimple_upload\">Load JSON by uploading file (simple)</a></li>\n" +
                "        <li><a href=\"/loadit/jsonsimple_link\">Load JSON by URL to file (simple)</a></li>\n" +
                "    </ul>\n" +
                "  </body>\n" +
                "</html>\n";
    }

    private String header() {
        return "<!DOCTYPE html>\n<html>\n" +
                "  <head>\n" +
                "      <meta charset=\"utf-8\">\n" +
                "      <title>Load It</title>\n" +
                "    </head>" +
                "  <body >\n" +
                "<table>" +
                "<td><image src=\"/vest.png\" title=\"Load It\" width=\"100\" height=\"100\"></td>" +
                "<td><font size=\"7\">Load It!</font></td>" +
                "</table>";
    }

    private String footer() {
        return "    <p><a href=\"/\"><font color=\"blue\">Back to index</font></a>\n" +
                "  </body>\n" +
                "</html>\n";
    }

    @RequestMapping("/loadit/csvsimple_upload")
    @ResponseBody
    public String csvSimpleUpload() {
        return csvsimple(null, true);
    }

    @RequestMapping("/loadit/csvsimple_link")
    @ResponseBody
    public String csvSimpleLink() {
        return csvsimple(null, false);
    }

    private String csvsimple(String message, boolean upload) {
        return  header() +
                ((null == message) ? "" : message) +
                DsbulkwebConfig.defaultInstance().simpleLoadFormCsv(upload) +
                footer();
    }

    @RequestMapping("/loadit/jsonsimple_upload")
    @ResponseBody
    public String jsonSimpleUpload() {
        return jsonsimple(null, true);
    }

    @RequestMapping("/loadit/jsonsimple_link")
    @ResponseBody
    public String jsonSimpleLink() {
        return jsonsimple(null, false);
    }

    private String jsonsimple(String message, boolean upload) {
        return  header() +
                ((null == message) ? "" : message) +
                DsbulkwebConfig.defaultInstance().simpleLoadFormJson(upload) +
                footer();
    }

    @RequestMapping("/loadit/loaditUpload")
    @ResponseBody
    public String loaditUpload(DsbulkwebConfig config) {
        String message = "<h2><font color=\"blue\">Successfully loaded " + config.getOriginalFilename() + "</font></h2>";
        if (!doloadit(config))
            message = "<h2><font color=\"red\">There was an error loading " + config.getOriginalFilename() + "</font></h2>";
        if (config.getConnector_name().equals("json"))
            return jsonsimple(message, true);
        // Default to CSV
        return csvsimple(message, true);
    }

    @RequestMapping("/loadit/loaditLink")
    @ResponseBody
    public String loaditLink(DsbulkwebConfig config) {
        String message = "<h2><font color=\"blue\">Successfully loaded " + config.getOriginalFilename() + "</font></h2>";
        if (!doloadit(config))
            message = "<h2><font color=\"red\">There was an error loading " + config.getOriginalFilename() + "</font></h2>";
        if (config.getConnector_name().equals("json"))
            return jsonsimple(message, false);
        // Default to CSV
        return csvsimple(message, false);
    }

    private boolean doloadit(DsbulkwebConfig config) {
        Path tmpDir = Files.createTempDir().toPath();
        String tmpDirPath = DsbulkwebConfig.pathToString(tmpDir);
        config.setLog_directory(tmpDirPath);
        System.err.println("Log dir: " + tmpDirPath);
        List<String> argsList = config.optionsList("load");
        String[] args = argsList.toArray(new String[0]);
        int retval = dsbulkloadit(args);
        config.deleteTmpFiles();
        try {
            boolean deleted = FileSystemUtils.deleteRecursively(tmpDir);
            System.err.println("Deleted temp directory? " + deleted);
        }
        catch (Exception e) {
            e.printStackTrace();
            tmpDir.toFile().deleteOnExit();
            //throw new RuntimeException("Couldn't delete directory (" + tmpDirPath + ")");
        }
        return (DataStaxBulkLoader.STATUS_OK == retval);
    }

    private int dsbulkloadit(String[] args) {
        DataStaxBulkLoader loader = new DataStaxBulkLoader(args);
        return loader.run();
    }
}
