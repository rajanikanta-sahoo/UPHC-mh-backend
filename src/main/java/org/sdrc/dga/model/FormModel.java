package org.sdrc.dga.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FormModel {

	Integer xFormId;
	int metaDataId;
	Integer timePeriodId;
	String formTitel;
}
