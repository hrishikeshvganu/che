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
package org.eclipse.che.api.factory.server;

import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.factory.shared.model.Factory;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Helps to convert to DTOs related to factory.
 *
 * @author Anton Korneta
 */
public final class DtoConverter {
    public static FactoryDto asDto(Factory factory) {
        return DtoFactory.newDto(FactoryDto.class);
    }

    private DtoConverter() {}
}
