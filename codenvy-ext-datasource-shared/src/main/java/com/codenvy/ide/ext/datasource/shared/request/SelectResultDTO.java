/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.datasource.shared.request;

import java.util.List;

import com.codenvy.dto.shared.DTO;

@DTO
public interface SelectResultDTO extends RequestResultDTO {

    static int TYPE = 1;

    SelectResultDTO withHeaderLine(List<String> line);

    SelectResultDTO withResultLines(List<List<String>> lines);

    SelectResultDTO withResultType(int type);

    SelectResultDTO withOriginRequest(String request);
}
