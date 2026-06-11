package io.github.nbplugins.claudecodegui.mcp.tools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openbeans.claude.netbeans.tools.Tool;

/**
 * MCP tool that adds a Maven dependency to a project's pom.xml.
 *
 * <p>Supports both specifying the {@code projectPath} directly and auto-detecting
 * the first open Maven project when {@code projectPath} is omitted.
 * The dependency is injected just before the closing {@code </dependencies>} tag
 * so the edit is minimal and preserves existing formatting.
 */
public class AddMavenDependency implements Tool<AddMavenDependency.Params, AddMavenDependency.Result> {

    private static final Logger LOGGER = Logger.getLogger(AddMavenDependency.class.getName());

    /** Creates a new instance of this tool. */
    public AddMavenDependency() {}

    @Override
    public String getName() {
        return "add_maven_dependency";
    }

    @Override
    public String getDescription() {
        return "Add a Maven dependency to a project's pom.xml. "
             + "Specify groupId, artifactId, version, and optionally scope and projectPath. "
             + "If projectPath is omitted the first open Maven project is used.";
    }

    @Override
    public Class<Params> getParameterClass() {
        return Params.class;
    }

    @Override
    public Result run(Params params) throws Exception {
        String pomPath = resolvePomPath(params.projectPath);
        if (pomPath == null) {
            return new Result(false, "Could not locate pom.xml. Provide projectPath or open a Maven project.", null);
        }

        File pomFile = new File(pomPath);
        if (!pomFile.exists()) {
            return new Result(false, "pom.xml not found: " + pomPath, null);
        }

        String content = Files.readString(pomFile.toPath(), StandardCharsets.UTF_8);

        if (alreadyContains(content, params.groupId, params.artifactId)) {
            return new Result(false,
                    "Dependency " + params.groupId + ":" + params.artifactId + " already present in pom.xml",
                    pomPath);
        }

        String updated = injectDependency(content, params);
        if (updated == null) {
            return new Result(false,
                    "Could not find <dependencies> block in pom.xml. Add one manually first.",
                    pomPath);
        }

        Files.writeString(pomFile.toPath(), updated, StandardCharsets.UTF_8);
        LOGGER.log(Level.INFO, "Added Maven dependency {0}:{1}:{2} to {3}",
                new Object[]{params.groupId, params.artifactId, params.version, pomPath});

        return new Result(true,
                "Added " + params.groupId + ":" + params.artifactId + ":" + params.version
                + (params.scope != null ? " (scope: " + params.scope + ")" : "") + " to pom.xml",
                pomPath);
    }

    // -------------------------------------------------------------------------

    private String resolvePomPath(String projectPath) {
        if (projectPath != null && !projectPath.isBlank()) {
            File dir = new File(projectPath);
            File pom = new File(dir, "pom.xml");
            return pom.exists() ? pom.getAbsolutePath() : null;
        }
        // Auto-detect: first open project with a pom.xml
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            FileObject dir = p.getProjectDirectory();
            if (dir != null) {
                FileObject pom = dir.getFileObject("pom.xml");
                if (pom != null) {
                    return new File(pom.getPath()).getAbsolutePath();
                }
            }
        }
        return null;
    }

    private boolean alreadyContains(String content, String groupId, String artifactId) {
        return content.contains("<groupId>" + groupId + "</groupId>")
            && content.contains("<artifactId>" + artifactId + "</artifactId>");
    }

    private String injectDependency(String content, Params p) {
        String closingTag = "</dependencies>";
        int idx = content.lastIndexOf(closingTag);
        if (idx < 0) {
            // Try to find <dependencies/> self-closing and expand it
            String selfClosing = "<dependencies/>";
            int sc = content.indexOf(selfClosing);
            if (sc < 0) return null;
            String block = "<dependencies>\n" + buildDependencyXml(p, "        ") + "    </dependencies>";
            return content.substring(0, sc) + block + content.substring(sc + selfClosing.length());
        }

        String snippet = buildDependencyXml(p, "        ");
        return content.substring(0, idx) + snippet + "    " + closingTag + content.substring(idx + closingTag.length());
    }

    private String buildDependencyXml(Params p, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("<dependency>\n");
        sb.append(indent).append("    <groupId>").append(p.groupId).append("</groupId>\n");
        sb.append(indent).append("    <artifactId>").append(p.artifactId).append("</artifactId>\n");
        sb.append(indent).append("    <version>").append(p.version).append("</version>\n");
        if (p.scope != null && !p.scope.isBlank()) {
            sb.append(indent).append("    <scope>").append(p.scope).append("</scope>\n");
        }
        sb.append(indent).append("</dependency>\n");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Parameter / result types
    // -------------------------------------------------------------------------

    /** Parameters for add_maven_dependency. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Params {
        /** Maven groupId of the dependency. */
        public String groupId;
        /** Maven artifactId of the dependency. */
        public String artifactId;
        /** Version of the dependency. */
        public String version;
        /** Optional Maven scope (compile, test, provided, runtime). */
        public String scope;
        /** Absolute path to the project directory containing pom.xml. Optional. */
        public String projectPath;
    }

    /** Result returned by add_maven_dependency. */
    public static class Result {
        /** Whether the operation succeeded. */
        public final boolean success;
        /** Human-readable status message. */
        public final String message;
        /** Absolute path to the pom.xml that was modified, or null. */
        public final String pomPath;

        /** Creates a new result. */
        public Result(boolean success, String message, String pomPath) {
            this.success = success;
            this.message = message;
            this.pomPath  = pomPath;
        }
    }
}
