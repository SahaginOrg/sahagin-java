package org.sahagin.main;

import java.io.File;

import org.sahagin.report.HtmlReport;
import org.sahagin.share.AcceptableLocales;
import org.sahagin.share.Config;
import org.sahagin.share.IllegalDataStructureException;
import org.sahagin.share.IllegalTestScriptException;
import org.sahagin.share.Logging;
import org.sahagin.share.SysMessages;
import org.sahagin.share.yaml.YamlConvertException;

public class SahaginMain {
    private static final String MSG_NO_COMMAND_LINE_ARGUMENT = "no command line arguments are specified";
    private static final String MSG_UNKNOWN_ACTION = "unknown action: %s";
    private static final String MSG_CONFIG_NOT_FOUND = "config file not found: %s";

    private enum Action {
        Report("report");

        private String value;

        private Action(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Action getEnum(String value) {
            for (Action action : values()) {
                if (action.getValue().equals(value)) {
                    return action;
                }
            }
            throw new IllegalArgumentException(String.format(MSG_UNKNOWN_ACTION, value));
        }

    }

    // first argument is action name (now "report" only), second argument is configuration file path
    public static void main(String[] args)
            throws YamlConvertException, IllegalDataStructureException, IllegalTestScriptException {
        if (args.length == 0) {
            throw new IllegalArgumentException(MSG_NO_COMMAND_LINE_ARGUMENT);
        }
        Action action = null;
        try {
            action = Action.getEnum(args[0]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(MSG_UNKNOWN_ACTION, args[0]));
        }
        String configFilePath;
        if (args.length <= 1) {
            configFilePath = "sahagin.yml";
        } else {
            configFilePath = args[1];
        }
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            throw new IllegalArgumentException(String.format(
                    MSG_CONFIG_NOT_FOUND, configFile.getAbsolutePath()));
        }
        Config config = Config.generateFromYamlConfig(configFile);
        Logging.setLoggerEnabled(config.isOutputLog());
        AcceptableLocales locales = AcceptableLocales.getInstance(config.getUserLocale());
        SysMessages.globalInitialize(locales);

        switch (action) {
        case Report:
            report(config);
            break;
        default:
            throw new RuntimeException("implementation error");
        }
    }

    private static void report(Config config)
            throws IllegalDataStructureException, IllegalTestScriptException {
        HtmlReport report = new HtmlReport();
        report.generate(config.getRootBaseReportIntermediateDataDir(),
                config.getRootBaseReportOutputDir());
    }

}
