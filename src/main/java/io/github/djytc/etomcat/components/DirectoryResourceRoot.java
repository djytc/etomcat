package io.github.djytc.etomcat.components;

import org.apache.catalina.WebResourceSet;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/**
 * User: alexkasko
 * Date: 5/9/17
 */
public class DirectoryResourceRoot extends StandardRoot {
    private final String docBaseDir;
    private final String webAppMount;

    public DirectoryResourceRoot(StandardContext ctx, String docBaseDir, String webAppMount) {
        super(ctx);
        this.docBaseDir = docBaseDir;
        this.webAppMount = webAppMount;
    }

    @Override
    protected WebResourceSet createMainResourceSet() {
        return new DirResourceSet(this, webAppMount, docBaseDir, "/");
    }
}

