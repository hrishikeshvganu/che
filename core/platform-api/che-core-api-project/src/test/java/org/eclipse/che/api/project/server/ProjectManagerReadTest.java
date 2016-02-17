/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


/**
 * @author gazarenkov
 */
public class ProjectManagerReadTest extends WsAgentTestBase {



    @Before
    public void setUp() throws Exception {

        super.setUp();


        new File(root, "/fromFolder").mkdir();
        new File(root, "/normal").mkdir();
        new File(root, "/normal/module").mkdir();


        List<ProjectConfigDto> projects = new ArrayList<>();
        projects.add(DtoFactory.newDto(ProjectConfigDto.class)
                               .withPath("/normal")
                               .withName("project1Name")
                               .withType("primary1"));

        projects.add(DtoFactory.newDto(ProjectConfigDto.class)
                               .withPath("/fromConfig")
                               .withName("")
                               .withType("primary1"));


        projects.add(DtoFactory.newDto(ProjectConfigDto.class)
                              .withPath("/normal/module")
                              .withName("project1Name")
                              .withType("primary1"));


        workspaceHolder = new TestWorkspaceHolder(projects);
        ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(new HashSet<>());
        projectTypeRegistry.registerProjectType(new PT1());

        ProjectHandlerRegistry projectHandlerRegistry = new ProjectHandlerRegistry(new HashSet<>());

        projectRegistry = new ProjectRegistry(workspaceHolder, vfsProvider, projectTypeRegistry);

        pm = new ProjectManager(vfsProvider, null, projectTypeRegistry, projectHandlerRegistry,
                                null, projectRegistry, fileWatcherNotificationHandler, fileTreeWatcher);
    }

//    @AfterClass
//    public static void remove() throws Exception {
//        File root = new File(FS_PATH);
//        FileUtils.deleteDirectory(root);
//    }

    @Test
    public void testInit() throws Exception {

        assertEquals(4, projectRegistry.getProjects().size());
        assertEquals(0, projectRegistry.getProject("/normal").getProblems().size());
        assertEquals(1, projectRegistry.getProject("/fromConfig").getProblems().size());
        assertEquals(1, projectRegistry.getProject("/fromFolder").getProblems().size());

    }


    @Test
    public void testNormalProject() throws Exception {

        assertEquals(4, pm.getProjects().size());
        assertNotNull(pm.getProject("/normal"));
        assertEquals("/normal", pm.getProject("/normal").getPath());
        assertEquals("project1Name", pm.getProject("/normal").getName());
        assertEquals(0, pm.getProject("/normal").getProblems().size());
    }

    @Test
    public void testProjectFromFolder() throws Exception {

        assertNotNull(pm.getProject("/fromFolder"));
        assertEquals("/fromFolder", pm.getProject("/fromFolder").getPath());
        assertEquals("fromFolder", pm.getProject("/fromFolder").getName());
        assertEquals(1, pm.getProject("/fromFolder").getProblems().size());
        assertEquals(BaseProjectType.ID, pm.getProject("/fromFolder").getProjectType().getId());
        assertEquals(11, pm.getProject("/fromFolder").getProblems().get(0).code);
    }

    @Test
    public void testProjectFromConfig() throws Exception {

        assertNotNull(pm.getProject("/fromConfig"));
        assertEquals("/fromConfig", pm.getProject("/fromConfig").getPath());
        assertEquals(1, pm.getProject("/fromConfig").getProblems().size());
        assertEquals("primary1", pm.getProject("/fromConfig").getProjectType().getId());
        assertEquals(10, pm.getProject("/fromConfig").getProblems().get(0).code);
    }

    @Test
    public void testInnerProject() throws Exception {

        String path = "/normal/module";
        assertNotNull(pm.getProject(path));
        assertEquals(0, pm.getProject(path).getProblems().size());
        assertEquals("primary1", pm.getProject(path).getProjectType().getId());


    }

    @Test
    public void testParentProject() throws Exception {

        assertEquals("/normal", projectRegistry.getParentProject("/normal").getPath());
        assertEquals("/normal", projectRegistry.getParentProject("/normal/some/path").getPath());
        assertEquals("/normal/module", projectRegistry.getParentProject("/normal/module/some/path").getPath());

        try {
            projectRegistry.getParentProject("/some/path");
            fail("NotFoundException expected");
        } catch (NotFoundException e) {}


    }

    @Test
    public void testSerializeProject() throws Exception {

        ProjectConfig config =
                           DtoConverter.toProjectConfig(pm.getProject("/fromConfig"), workspaceHolder.getWorkspace().getId(), null);

        assertEquals("/fromConfig", config.getPath());
        assertEquals("primary1", config.getType());

    }


    @Test
    public void testDoNotReturnNotInitializedAttribute() throws Exception {

        // SPEC:
        // Not initialized attributes should not be returned

        assertEquals(1, projectRegistry.getProject("/normal").getAttributes().size());

    }



    @Test
    public void testEstimateProject() throws Exception {

    }

    @Test
    public void testResolveSources() throws Exception {

    }

    @Test
    public void testIfConstantAttrIsAccessible() throws Exception {

        assertEquals("my constant", pm.getProject("/normal").getAttributeEntries().get("const1").getString());

    }



}
