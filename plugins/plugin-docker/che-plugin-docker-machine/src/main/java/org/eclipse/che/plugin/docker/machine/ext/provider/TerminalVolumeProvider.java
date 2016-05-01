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
package org.eclipse.che.plugin.docker.machine.ext.provider;

import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.docker.machine.WindowsHostUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

/**
 * Provides volumes configuration of machine for terminal
 *
 * <p>On Windows MUST be locate in "user.home" directory in case limitation windows+docker.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class TerminalVolumeProvider implements Provider<Set<String>> {

    private static final String CONTAINER_TARGET = ":/mnt/che/terminal:ro,Z";
    private static final String TERMINAL         = "terminal";
    private static final Logger LOG              = LoggerFactory.getLogger(TerminalVolumeProvider.class);

    private Set<String> terminalVolume;

    @Inject
    public TerminalVolumeProvider(@Named("machine.server.terminal.path_to_archive.linux_amd64") String terminalArchivePath) {
        if (SystemInfo.isWindows()) {
            try {
                final Path cheHome = WindowsHostUtils.ensureCheHomeExist();
                final Path terminalPath = cheHome.resolve(TERMINAL);
                IoUtil.copy(Paths.get(terminalArchivePath).toFile(), terminalPath.toFile(), null, true);
                terminalVolume = Collections.singleton(terminalPath.toString() + CONTAINER_TARGET);
            } catch (IOException e) {
                LOG.warn(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            terminalVolume = Collections.singleton(terminalArchivePath + CONTAINER_TARGET);
        }
    }

    @Override
    public Set<String> get() {
        return terminalVolume;
    }
}
