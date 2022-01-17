/**
 * 
 */
package org.sdrc.dga.repository;

import java.util.List;

import org.sdrc.dga.domain.Program;

/**
 * @author Harsh Pratyush (harsh@sdrc.co.in)
 *
 */
public interface ProgrammRepository {

	Program findByProgramId(int i);
	
	Program findByProgramName(String name);
	
	List<Program> findByProgramIdIn(List<Integer> ids);

}
