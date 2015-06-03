package org.caltesting.maven.sphinx;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Sphinx Mojo
 *
 * @author tomdz & balasridhar
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.SITE, requiresReports = true)
public class SphinxMojo extends AbstractMojo implements MavenReport {
    /**
     * The maven project object.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * The base directory of the project.
     */
    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    private File basedir;

    /**
     * The directory containing the sphinx doc source.
     */
    @Parameter(property = "sphinx.srcDir", defaultValue = "${basedir}/src/site/sphinx", required = true)
    private File sourceDirectory;

    /**
     * Directory where reports will go.
     */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}", required = true, readonly = true)
    private File outputDirectory;
    
    /**
     * The directory for sphinx' source.
     */
    @Parameter(property = "sphinx.sphinxSrcDir", defaultValue = "${project.build.directory}/sphinx", required = true, readonly = true)
    private File sphinxSourceDirectory;

    /**
     * The builder to use. See <a href="http://sphinx.pocoo.org/man/sphinx-build.html?highlight=command%20line">sphinx-build</a>
     * for a list of supported builders.
     */
    @Parameter(property = "sphinx.builder", required = true, alias = "builder", defaultValue = "html")
    private String builder;

    /**
     * The <a href="http://sphinx.pocoo.org/markup/misc.html#tags">tags</a> to pass to the sphinx build.
     */
    @Parameter(property = "sphinx.tags", alias = "tags")
    private List<String> tags;

    /**
     * Whether Sphinx should generate verbose output.
     */
    @Parameter(property = "sphinx.verbose", defaultValue = "true", required = true, alias = "verbose")
    private boolean verbose;

    /**
     * Whether Sphinx should treat warnings as errors.
     */
    @Parameter(property = "sphinx.warningAsErrors", defaultValue = "false", required = true, alias = "warningAsErrors")
    private boolean warningsAsErrors;

    /**
     * Whether Sphinx should generate output for all files instead of only the changed ones.
     */
    @Parameter(property = "sphinx.force", defaultValue = "false", required = true, alias = "force")
    private boolean force;

    /** Sphinx Executor. */
    private final SphinxRunner sphinxRunner;

    /**
     * Default Constructor.
     */
    public SphinxMojo() {
        sphinxRunner = new SphinxRunner(getLog());
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            if (verbose) {
                getLog().info("Running sphinx on " + sourceDirectory.getAbsolutePath() + ", output will be placed in "
                        + outputDirectory.getAbsolutePath());
            }

            String[] args = getSphinxRunnerCmdLine();
            int result;
            try {
                result = sphinxRunner.runSphinx(args, sphinxSourceDirectory, verbose);
            } catch (Exception ex) {
                throw new MavenReportException("Could not generate documentation", ex);
            }
            if (result != 0) {
                throw new MavenReportException("Sphinx report generation failed");
            }
        } catch (MavenReportException ex) {
            throw new MojoExecutionException("Failed to run the report", ex);
        }
    }

    @Override
    public void generate(Sink sink, Locale locale) throws MavenReportException {
        try {
            this.execute();
        } catch (Exception ex) {
            throw new MavenReportException("Error Generating Report", ex);
        }
    }

    @Override
    public String getOutputName() {
        return "Sphinx";
    }

    @Override
    public String getCategoryName() {
        return "Sphinx";
    }

    @Override
    public String getName(Locale locale) {
        return "Sphinx";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Sphinx";
    }

    @Override
    public void setReportOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public File getReportOutputDirectory() {
        return this.outputDirectory;
    }

    @Override
    public boolean isExternalReport() {
        return true;
    }

    @Override
    public boolean canGenerateReport() {
        return true;
    }

    /**
     * Build the Sphinx Command line options.
     *
     * @return
     */
    private String[] getSphinxRunnerCmdLine() {
        List<String> args = new ArrayList<String>();

        if (verbose) {
            args.add("-v");
        } else {
            args.add("-Q");
        }

        if (warningsAsErrors) {
            args.add("-W");
        }

        if (force) {
            args.add("-a");
            args.add("-E");
        }

        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                args.add("-t");
                args.add(tag);
            }
        }

        args.add("-b");
        args.add(builder);

        args.add("-n");
        args.add(sourceDirectory.getAbsolutePath());
        args.add(outputDirectory.getAbsolutePath() + File.separator + builder);
        return args.toArray(new String[args.size()]);
    }

}
