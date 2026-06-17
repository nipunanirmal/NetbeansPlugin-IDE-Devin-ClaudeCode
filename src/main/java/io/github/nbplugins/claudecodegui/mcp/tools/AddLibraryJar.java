package io.github.nbplugins.claudecodegui.mcp.tools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openbeans.claude.netbeans.tools.Tool;

/**
 * MCP tool that adds a JAR library to a classic Ant/NetBeans project.
 *
 * <p>Copies (or registers without copying) the JAR into the project's {@code lib/}
 * folder and adds it to {@code javac.classpath} in
 * {@code nbproject/project.properties}.
 *
 * <p>Can also be used for Maven projects by providing only {@code jarPath} with
 * {@code copyToLib=false} — in that case nothing is written and the user is
 * advised to add the dependency via {@code add_maven_dependency} instead.
 */
public class AddLibraryJar implements Tool<AddLibraryJar.Params, AddLibraryJar.Result> {

    private static final Logger LOGGER = Logger.getLogger(AddLibraryJar.class.getName());

    /** Creates a new instance of this tool. */
    public AddLibraryJar() {}

    @Override
    public String getName() {
        return "add_library_jar";
    }

    @Override
    public String getDescription() {
        return "Add a JAR library to an Ant/NetBeans project. "
             + "Copies the JAR into the project lib/ folder and registers it in "
             + "nbproject/project.properties (javac.classpath). "
             + "Provide projectPath (directory of the .nbproject folder) and jarPath "
             + "(absolute path to the JAR to add). Set copyToLib=false to only register "
             + "an existing JAR without copying. For Maven projects use add_maven_dependency instead.";
    }

    @Override
    public Class<Params> getParameterClass() {
        return Params.class;
    }

    @Override
    public Result run(Params params) throws Exception {
        String projectDir = resolveProjectDir(params.projectPath);
        if (projectDir == null) {
            return new Result(false,
                    "Could not locate an Ant/NetBeans project. "
                    + "Provide projectPath or open an Ant project. "
                    + "For Maven projects use add_maven_dependency.",
                    null, null);
        }

        File nbprojectDir = new File(projectDir, "nbproject");
        if (!nbprojectDir.isDirectory()) {
            return new Result(false,
                    "nbproject/ directory not found in: " + projectDir
                    + ". This tool is for Ant/NetBeans projects.",
                    projectDir, null);
        }

        File jarFile = new File(params.jarPath);
        if (!jarFile.exists() || !jarFile.isFile()) {
            return new Result(false, "JAR not found: " + params.jarPath, projectDir, null);
        }

        // Determine final JAR path inside the project
        boolean doCopy = params.copyToLib == null || params.copyToLib;
        File targetJar;
        if (doCopy) {
            File libDir = new File(projectDir, "lib");
            libDir.mkdirs();
            targetJar = new File(libDir, jarFile.getName());
            if (targetJar.exists()) {
                return new Result(false,
                        "JAR already exists in lib/: " + targetJar.getName(),
                        projectDir, targetJar.getAbsolutePath());
            }
            Files.copy(jarFile.toPath(), targetJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.log(Level.INFO, "Copied JAR to {0}", targetJar.getAbsolutePath());
        } else {
            targetJar = jarFile;
        }

        // Classpath entry — use relative path when JAR is inside the project
        String classpathEntry = toRelativePath(new File(projectDir), targetJar);

        // Update nbproject/project.properties
        File propsFile = new File(nbprojectDir, "project.properties");
        String updated = updateClasspath(propsFile, classpathEntry);
        if (updated == null) {
            return new Result(false,
                    "Could not update nbproject/project.properties",
                    projectDir, targetJar.getAbsolutePath());
        }
        Files.writeString(propsFile.toPath(), updated, StandardCharsets.ISO_8859_1);

        LOGGER.log(Level.INFO, "Added library {0} to project {1}",
                new Object[]{classpathEntry, projectDir});

        return new Result(true,
                "Added " + jarFile.getName() + " to project classpath."
                + (doCopy ? " JAR copied to lib/." : " JAR registered without copying.")
                + " Reload the project in NetBeans to apply.",
                projectDir, targetJar.getAbsolutePath());
    }

    // -------------------------------------------------------------------------

    private String resolveProjectDir(String projectPath) {
        if (projectPath != null && !projectPath.isBlank()) {
            File dir = new File(projectPath);
            return dir.isDirectory() ? dir.getAbsolutePath() : null;
        }
        // Auto-detect: first open project with nbproject/
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            FileObject dir = p.getProjectDirectory();
            if (dir != null) {
                FileObject nb = dir.getFileObject("nbproject");
                if (nb != null) {
                    return new File(dir.getPath()).getAbsolutePath();
                }
            }
        }
        return null;
    }

    private String toRelativePath(File base, File target) {
        try {
            Path rel = base.getCanonicalFile().toPath().relativize(target.getCanonicalFile().toPath());
            return rel.toString().replace('\\', '/');
        } catch (IOException e) {
            return target.getAbsolutePath().replace('\\', '/');
        }
    }

    /**
     * Adds {@code entry} to the {@code javac.classpath} property in the given
     * {@code project.properties} file. Creates the property if absent.
     *
     * @return updated file contents, or {@code null} on read error
     */
    private String updateClasspath(File propsFile, String entry) {
        String content;
        try {
            if (propsFile.exists()) {
                content = Files.readString(propsFile.toPath(), StandardCharsets.ISO_8859_1);
            } else {
                content = "";
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not read project.properties", e);
            return null;
        }

        // Check already present
        if (content.contains(entry)) {
            return content; // idempotent
        }

        // Find javac.classpath=...  (may span multiple continuation lines ending in \)
        String key = "javac.classpath=";
        int idx = content.indexOf(key);
        if (idx < 0) {
            // Append new property
            String newline = content.endsWith("\n") ? "" : "\n";
            return content + newline + key + "\\\n    " + entry + "\n";
        }

        // Find end of the property value (skip continuation lines)
        int valueStart = idx + key.length();
        int end = valueStart;
        while (end < content.length()) {
            int nl = content.indexOf('\n', end);
            if (nl < 0) { end = content.length(); break; }
            if (nl > 0 && content.charAt(nl - 1) == '\\') {
                end = nl + 1;
            } else {
                end = nl + 1;
                break;
            }
        }

        // Append entry to the value
        String existing = content.substring(valueStart, end);
        String trimmed = existing.stripTrailing();
        String appended;
        if (trimmed.isEmpty()) {
            appended = "\\\n    " + entry + "\n";
        } else {
            // existing value might already end with \ or not
            if (trimmed.endsWith("\\")) {
                appended = trimmed + "\n    " + entry + "\n";
            } else {
                appended = trimmed + ":\\\n    " + entry + "\n";
            }
        }

        return content.substring(0, valueStart) + appended + content.substring(end);
    }

    // -------------------------------------------------------------------------

    /** Parameters for add_library_jar. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Params {
        /** Absolute path to the JAR file to add. */
        public String jarPath;
        /** Absolute path to the project directory (contains nbproject/). Optional. */
        public String projectPath;
        /** Whether to copy the JAR into the project lib/ folder. Defaults to true. */
        public Boolean copyToLib;
    }

    /** Result returned by add_library_jar. */
    public static class Result {
        /** Whether the operation succeeded. */
        public final boolean success;
        /** Human-readable status message. */
        public final String message;
        /** Absolute path to the project directory. */
        public final String projectPath;
        /** Absolute path to the JAR as registered. */
        public final String jarPath;

        /** Creates a new result. */
        public Result(boolean success, String message, String projectPath, String jarPath) {
            this.success     = success;
            this.message     = message;
            this.projectPath = projectPath;
            this.jarPath     = jarPath;
        }
    }
}
