/*
 * Author Emeric Kwemou on 30.01.2005 this small package provides an interface to generate options of a jplag.Program
 * instance all type of options will be derived from the abstract class Options. A simple implementation is for example
 * a class that initializes command line options.
 */
package jplag.options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jplag.ExitException;
import jplag.Language;
import jplag.Program;
import jplag.ProgramI;
import jplag.clustering.SimilarityMatrix;
import jplag.filter.Filter;

/**
 * @Author Emeric Kwemou 30.01.2005
 */
public abstract class Options {
    protected String langs = "jplag.options.util.messages_en";

    protected boolean suffixes_set;

    protected boolean min_token_match_set;

    public static final int MIN_CLUSTER = 1;
    public static final int MAX_CLUSTER = 2;
    public static final int AVR_CLUSTER = 3;

    // Program OPTIONS
    public boolean verbose_quiet;

    public boolean verbose_long;

    public boolean verbose_parser;

    public boolean verbose_details;

    public String sub_dir = null; // source folders (-s argument)

    public String root_dir = null;

    public String original_dir = null;

    public String title = "";

    public String output_file = null; // destination of the parser log (-o argument)

    public String exclude_file = null; // exclusion file, like a gitignore (-x argument)

    public String include_file = null; // (-i argument)

    public boolean read_subdirs; // if sub directories should be read (-S argument)

    public int store_matches = 30;

    public boolean store_percent; // is the number "store_matches"

    // a percentage?
    public static final int MAX_RESULT_PAIRS = 1000;

    public String result_dir = "result"; // where the results are exported to as a web page (-r)
    
    public boolean generateWebPage;

    public String helper[] = new String[1];

    public static final int COMPMODE_NORMAL = 0;
    public static final int COMPMODE_REVISION = 1;

    public int comparisonMode = COMPMODE_NORMAL;

    public int min_token_match;

    public String[] suffixes; // TODO TS: This should probably be a list

    public boolean exp; // EXPERIMENT

    public boolean diff_report; // special "diff" report

    public Filter filter = null;

    public String filtername = "."; // == no filter

    public String basecode = ""; // common framework given to the students (-bc argument)

    // "Ronald Kostoff" specials
    public boolean externalSearch;

    public boolean skipParse;

    public boolean clustering;

    public boolean useBasecode;

    public String languageName = null;

    public String countryTag = "en";

    public float[] threshold = null;

    public int[] themewords = { 15 };

    public int clusterType = 0;

    public int compare = 0; // 0 = deactivated

    public SimilarityMatrix similarity = null;

    private Language language;

    // compare list of files options
    public boolean fileListMode;

    public List<String> fileList = new ArrayList<String>();

    // "FINAL" OPTIONS
    public boolean debugParser;

    // END OPTIONS

    public static final int DEFAULT_STATE = 50;
    public static final int PARSING = 100;
    public static final int PARSING_WARNINGS = 101;
    public static final int COMPARING = 200;
    public static final int GENERATING_RESULT_FILES = 250;
    public static final int SUBMISSION_ABORTED = 405;

    private int int_progress = 0;

    private int state = DEFAULT_STATE;

    private boolean forceStop;

    public void forceProgramToStop() {
        forceStop = true;
    }

    public boolean isForceStop() {
        return forceStop;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
        if (!min_token_match_set)
            this.min_token_match = this.getLanguage().min_token_match();
        if (!suffixes_set)
            this.suffixes = this.getLanguage().suffixes();
    }

    public int getProgress() {
        return int_progress;
    }

    public int getState() {
        return state;
    }

    public void setProgress(int progress) throws jplag.ExitException {
        int_progress = progress;
        if (forceStop)
            throw new jplag.ExitException("Submission aborted", SUBMISSION_ABORTED);
    }

    public void setState(int state) {
        this.state = state;
    }

    // TODO control how the exclusion file is handled by the Program

    public static void usage() {
        System.out.print(Program.name_long + ", Copyright (c) 2004-2017 KIT - IPD Tichy, Guido Malpohl, and others.\n"
                + "Usage: JPlag [ options ] <root-dir> [-c file1 file2 ...]\n"
                + " <root-dir>        The root-directory that contains all submissions.\n\n" + "options are:\n" + " -v[qlpd]        (Verbose)\n"
                + "                 q: (Quiet) no output\n" + "                 l: (Long) detailed output\n"
                + "                 p: print all (p)arser messages\n" + "                 d: print (d)etails about each submission\n"
                + " -d              (Debug) parser. Non-parsable files will be stored.\n"
                + " -s <dir>        Look in directories <root-dir>/*/<dir> for programs.\n" + "                 (default: <root-dir>/*)\n"
                + " -S              (Subdirs) Look at files in subdirs too (default: activated)\n\n"
                + " -p <suffixes>   <suffixes> is a comma-separated list of all filename suffixes\n"
                + "                 that are included. (\"-p ?\" for defaults)\n\n"
                + " -o <file>       (Output) The Parserlog will be saved to <file>\n"
                + " -x <file>       (eXclude) All files named in <file> will be ignored\n"
                + " -t <n>          (Token) Tune the sensitivity of the comparison. A smaller\n" + "                 <n> increases the sensitivity.\n"
                + " -m <n>          (Matches) Number of matches that will be saved (default:20)\n"
                + " -m <p>%         All matches with more than <p>% similarity will be saved.\n"
                + " -r <dir>        (Result) Name of directory in which the web pages will be\n" + "                 stored (default: result)\n"
                + " -bc <dir>       Name of the directory which contains the basecode (common framework)\n"
                + " -c [files]      Compare a list of files. Should be the last one.\n"
                + " -l <language>   (Language) Supported Languages:\n                 ");
        System.out.println(LanguageLiteral.availableLanguages());
    }

    /**
     * Prints detailed information on all available languages. Does also give information on instantiation problems.
     */
    protected static void printAllLanguages() throws ExitException{
        for (LanguageLiteral language : LanguageLiteral.values()) {
            try {
                Language langClass = (Language) Class.forName(language.getClassName()).getConstructor(ProgramI.class).newInstance(new Program());
                System.out.println(language.getAbbreviation());
                String suffixes[] = langClass.suffixes();
                System.out.print(" Suffixes: ");
                for (int j = 0; j < suffixes.length; j++) {
                    System.out.print(suffixes[j] + (j + 1 < suffixes.length ? ", " : "\n"));
                }
                System.out.println(" Min token match: " + langClass.min_token_match());
            } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException exception) {
                System.err.println("Could not find " + language + " " + exception.getMessage());
            }
        }
        System.exit(0);
    }

    public String getClusterTyp() {
        if (this.clusterType == MIN_CLUSTER)
            return "min";
        else if (this.clusterType == MAX_CLUSTER)
            return "max";
        else if (this.clusterType == AVR_CLUSTER)
            return "avr";
        else
            return "";
    }

    public String getCountryTag() {
        return this.countryTag;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * This method checks whether the base code directory value is valid
     */
    public void checkBasecodeOption() throws jplag.ExitException {
        if (useBasecode) {
            if (basecode == null || basecode.equals("")) {
                throw new ExitException("Basecode option used but none " + "specified!", ExitException.BAD_PARAMETER);
            }
            String fullPath = root_dir + File.separator + basecode;
            if (!(new File(root_dir)).exists()) {
                throw new ExitException("Root directory \"" + root_dir + "\" doesn't exist!", ExitException.BAD_PARAMETER);
            }
            if (!new File(fullPath).exists()) {  // Basecode dir doesn't exist.
                throw new ExitException("Basecode directory \"" + fullPath + "\" doesn't exist!", ExitException.BAD_PARAMETER);
            }
            if (sub_dir != null && sub_dir.length() != 0) {
                if (!new File(fullPath, sub_dir).exists()) {
                    throw new ExitException("Basecode directory doesn't contain" + " the subdirectory \"" + sub_dir + "\"!",
                            ExitException.BAD_PARAMETER);
                }
            }
            System.out.println("Basecode directory \"" + fullPath + "\" will be used");
        }
    }

}
