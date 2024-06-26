/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.admin.controller;

import com.alibaba.nacos.common.utils.DateFormatUtils;
import org.apache.shenyu.admin.aspect.annotation.RestApi;
import org.apache.shenyu.admin.model.result.ShenyuAdminResult;
import org.apache.shenyu.admin.service.ConfigsService;
import org.apache.shenyu.admin.utils.ShenyuResultMessage;
import org.apache.shenyu.common.constant.ExportImportConstants;
import org.apache.shenyu.common.exception.CommonErrorCode;
import org.apache.shenyu.common.exception.ShenyuException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * this is configs controller.
 */
@RestApi("/configs")
public class ConfigsExportImportController {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigsExportImportController.class);

    /**
     * The config service.
     */
    private final ConfigsService configsService;

    public ConfigsExportImportController(final ConfigsService configsService) {
        this.configsService = configsService;
    }

    /**
     * Export all configs.
     *
     * @param response response
     * @return the shenyu result
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportConfigs(final HttpServletResponse response) {
        ShenyuAdminResult result = configsService.configsExport();
        if (!Objects.equals(CommonErrorCode.SUCCESSFUL, result.getCode())) {
            throw new ShenyuException(result.getMessage());
        }
        HttpHeaders headers = new HttpHeaders();
        String fileName = generateFileName();
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        headers.add("Content-Disposition", "attachment;filename=" + fileName);
        return new ResponseEntity<>((byte[]) result.getData(), headers, HttpStatus.OK);
    }

    /**
     * generate export file name.
     *
     * @return fileName
     */
    private String generateFileName() {
        return ExportImportConstants.EXPORT_CONFIG_FILE_NAME + DateFormatUtils.format(new Date(), ExportImportConstants.EXPORT_CONFIG_FILE_NAME_DATE_FORMAT)
                + ExportImportConstants.EXPORT_CONFIG_FILE_NAME_EXT;
    }

    /**
     * Import configs.
     *
     * @param file config file
     * @return shenyu admin result
     */
    @PostMapping("/import")
    public ShenyuAdminResult importConfigs(final MultipartFile file) {
        if (Objects.isNull(file)) {
            return ShenyuAdminResult.error(ShenyuResultMessage.PARAMETER_ERROR);
        }
        try {
            return configsService.configsImport(file.getBytes());
        } catch (IOException e) {
            LOG.error("parsing data failed", e);
            return ShenyuAdminResult.error(ShenyuResultMessage.PARAMETER_ERROR);
        }
    }

}
