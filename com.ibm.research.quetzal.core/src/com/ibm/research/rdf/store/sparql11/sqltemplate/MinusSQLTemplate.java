package com.ibm.research.rdf.store.sparql11.sqltemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.research.rdf.store.Context;
import com.ibm.research.rdf.store.Store;
import com.ibm.research.rdf.store.config.Constants;
import com.ibm.research.rdf.store.sparql11.model.Variable;
import com.ibm.research.rdf.store.sparql11.planner.PlanNode;

public class MinusSQLTemplate extends AbstractSQLTemplate {
	PlanNode left;
	PlanNode right;
	
	public MinusSQLTemplate(String templateName, PlanNode planNode,
			Store store, Context ctx, STPlanWrapper wrapper, PlanNode left, PlanNode right) {
		super(templateName, store, ctx, wrapper, planNode);
		this.left = left;
		this.right = right;
		wrapper.mapPlanNode(planNode);
	}

	@Override
	Set<SQLMapping> populateMappings() {
		HashSet<SQLMapping> mappings = new HashSet<SQLMapping>();
		
		List<String> qidSqlParam = new LinkedList<String>();
		qidSqlParam.add(getQIDMapping());
		SQLMapping qidMapping=new SQLMapping("sql_id", qidSqlParam,null);
		mappings.add(qidMapping);
		
		Map<String,String> projectSQLClauseMapping = getLeftProjectMapping();		
		List<String> projectedVariables = new LinkedList<String>();
		projectedVariables.addAll(projectSQLClauseMapping.keySet());
		Collections.sort(projectedVariables);		
		List<String> projectSqlParams = new LinkedList<String>();
		for(String pVar : projectedVariables){
			projectSqlParams.add(projectSQLClauseMapping.get(pVar)+" AS "+pVar);
		}	
		SQLMapping pMapping=new SQLMapping("left_project", projectSqlParams,null);
		mappings.add(pMapping);
		
		SQLMapping tMapping=new SQLMapping("left_target", getLeftTargetMapping(),null);
		mappings.add(tMapping);		
			
		Map<String,String> projectRightSQLClauseMapping = getRightProjectMapping();
		List<String> rightProjectSqlParams = new LinkedList<String>();		
		List<String> rightProjectedVariables = new LinkedList<String>();
		rightProjectedVariables.addAll(projectRightSQLClauseMapping.keySet());
		Collections.sort(rightProjectedVariables);		
		for(String pVar : rightProjectedVariables){
			rightProjectSqlParams.add(projectRightSQLClauseMapping.get(pVar)+" AS "+pVar);
		}
		SQLMapping pRightMapping=new SQLMapping("right_project", rightProjectSqlParams,null);
		mappings.add(pRightMapping);
		
		SQLMapping tRightMapping=new SQLMapping("right_target", getRightTargetMapping(),null);
		mappings.add(tRightMapping);		
			
		return mappings;
	}
	
	Map<String,String> getLeftProjectMapping(){
		Map<String,String> projectMapping = new HashMap<String,String>();
		Set<Variable> minusVariables=planNode.getProducedVariables();
		String leftSQLCte = wrapper.getPlanNodeCTE(left, false); 
		Set<Variable> leftAvailable = left.getAvailableVariables();
		Set<Variable> iriBoundVariables = wrapper.getIRIBoundVariables();
		for(Variable v : minusVariables){
			if(leftAvailable != null){
				if(leftAvailable.contains(v)){
					String vPredName = wrapper.getPlanNodeVarMapping(left,v.getName());
					projectMapping.put(v.getName(),leftSQLCte+"."+vPredName);
					
						if(!iriBoundVariables.contains(v)){
							projectMapping.put(v.getName()+Constants.TYP_COLUMN_SUFFIX_IN_SPARQL_RS,leftSQLCte+"."+vPredName+Constants.TYP_COLUMN_SUFFIX_IN_SPARQL_RS);
						}		
				}
				else{
					projectMapping.put(v.getName(),"null");
					if(!iriBoundVariables.contains(v)){
						projectMapping.put(v.getName()+Constants.TYP_COLUMN_SUFFIX_IN_SPARQL_RS,"null");
					}
				}
			}
		}
		return projectMapping;
	}
	
	Map<String,String> getRightProjectMapping(){
		Map<String,String> projectMapping = new HashMap<String,String>();
		Set<Variable> unionVariables=planNode.getProducedVariables();
		String rightSQLCte = wrapper.getPlanNodeCTE(right, false); 
		Set<Variable> rightAvailable = right.getAvailableVariables();
		Set<Variable> iriBoundVariables = wrapper.getIRIBoundVariables();
		for(Variable v : unionVariables){
			if(rightAvailable != null)
				if(rightAvailable.contains(v)){
					String vPredName = wrapper.getPlanNodeVarMapping(right,v.getName());
					projectMapping.put(v.getName(),rightSQLCte+"."+vPredName);
					if(!iriBoundVariables.contains(v)){
						projectMapping.put(v.getName()+Constants.TYP_COLUMN_SUFFIX_IN_SPARQL_RS,rightSQLCte+"."+vPredName+Constants.TYP_COLUMN_SUFFIX_IN_SPARQL_RS);
					}					
				}
				else{
					projectMapping.put(v.getName(),"null");
					if(!iriBoundVariables.contains(v)){
						projectMapping.put(v.getName()+Constants.TYP_COLUMN_SUFFIX_IN_SPARQL_RS,"null");
					}
				}
		}
		return projectMapping;
	}
	
	List<String> getLeftTargetMapping(){
		List<String> targetMapping = new LinkedList<String>();
		targetMapping.add(wrapper.getPlanNodeCTE(left, true));
		return targetMapping;
	}
	
	List<String> getRightTargetMapping(){
		List<String> targetMapping = new LinkedList<String>();
		targetMapping.add(wrapper.getPlanNodeCTE(right, true));
		return targetMapping;
	}	
	
}