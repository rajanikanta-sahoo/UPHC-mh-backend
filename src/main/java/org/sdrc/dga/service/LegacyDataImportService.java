package org.sdrc.dga.service;

import org.springframework.web.multipart.MultipartFile;

public interface LegacyDataImportService {

	public Boolean importLegacyData(int timePeriod, int formId,MultipartFile file);
}
