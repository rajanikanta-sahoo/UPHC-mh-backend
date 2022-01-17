package org.sdrc.dga.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class InitalDataModel {

	List<AreaModel> districts;
	List<TimePeriodModel> timePeriods;
	List<FormModel> forms;
}
