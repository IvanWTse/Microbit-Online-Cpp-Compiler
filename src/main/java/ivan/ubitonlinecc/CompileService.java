package ivan.ubitonlinecc;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Random;

@RestController
public class CompileService {
    @RequestMapping(value = "/compile", method = RequestMethod.POST)
    public synchronized ResponseEntity<CompileMsgBody> compile(@RequestParam String source) throws IOException, InterruptedException {
        CompileMsgBody ret = new CompileMsgBody();
        //Generate a file name
        File outputFile = null;
        do {
            char[] fileNameBytes = new char[10];
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                fileNameBytes[i] = (char) ('a' + random.nextInt(26));
            }
            String fileName = String.valueOf(fileNameBytes);
            outputFile = new File("webapps/uBitOnlineCC/WEB-INF/classes/static/output/" + fileName + ".hex");
        } while (!outputFile.createNewFile());

        //Write source file from parameter
        FileWriter fw = new FileWriter("/var/lib/tomcat9/webapps/uBitCC/source/main.cpp");
        fw.write(source);
        fw.close();

        // Execute build
        Process buildExec = Runtime.getRuntime().exec("/bin/sh ./build.sh", null, new File("/var/lib/tomcat9/webapps/uBitCC"));
        int returnVal = buildExec.waitFor();
        BufferedReader errStream = new BufferedReader(new InputStreamReader(buildExec.getErrorStream()));
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(buildExec.getInputStream()));
        String errLine, inputLine;
        while ((errLine = errStream.readLine()) != null | (inputLine = inputStream.readLine()) != null) {
            if (errLine != null) {
                System.out.println(errLine);
                if (errLine.startsWith("/var/lib/tomcat9/webapps/uBitCC/source/main.cpp:")) {
                    System.out.println("the line: " + errLine);
                    if (errLine.split(":").length > 3) {
                        int lineNo = Integer.parseInt(errLine.split(":")[1]);
                        ret.getErrorLines().put(lineNo, errLine.substring("/var/lib/tomcat9/webapps/uBitCC/source/main.cpp:10:1: error:".length() + 1));
                        System.out.println("errorLine: " + lineNo);
                    }
                }
            }
        }
        errStream.close();
        inputStream.close();
        if (returnVal == 0) {
            Runtime.getRuntime().exec("cp /var/lib/tomcat9/webapps/uBitCC/MICROBIT.hex " + outputFile.getAbsolutePath());
            ret.setFilePath(outputFile.getName());
            return new ResponseEntity<>(ret, null, 200);
        }
        return new ResponseEntity<>(ret, null, 50001);
    }
}
