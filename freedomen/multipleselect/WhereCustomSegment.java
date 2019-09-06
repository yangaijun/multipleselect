package com.freedomen.multipleselect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WhereCustomSegment { 
	
	private TableEntity tableEntity;
	
	private List<String> segmentSql;
	
	private Map<String, Object> parameter = null;
	
	private String parameterPrefixName;
	
	private String currentOpreatioalType = "AND"; 
	
	private final Integer leftDivision = 0x0001;
	
	private final Integer rightDivision = 0x0002;
	
	public WhereCustomSegment(TableEntity tableEntity) {
		segmentSql = new ArrayList<>();
		this.tableEntity = tableEntity; 
		parameter = new HashMap<>();
		parameterPrefixName = "custom_" + tableEntity.getNickName() + "_";
	}
	//create same segment [tableName].[tableCoumn] as: 'and table1.table_column1',if no column be found,return null;
	private StringBuffer getPublicSegment(String column) {
		StringBuffer sb = new StringBuffer();
		if (!tableEntity.getAllEntityColumns().contains(column)) {
			(new Exception("no column " + column + " be found in table " + tableEntity.getNickName())).printStackTrace();
			return null;
		}
		
		try {
			if (segmentSql.get(segmentSql.size() - 1).indexOf("(") == -1 
					|| (segmentSql.get(segmentSql.size() - 1).indexOf("(") != -1 && segmentSql.get(segmentSql.size() - 1).indexOf(")") != -1))
				sb.append(currentOpreatioalType);
		} catch (Exception e) {
			sb.append(currentOpreatioalType);
		}
		
		sb.append(" ")
		  .append(tableEntity.getNickName()).append(".")
		  .append(tableEntity.getAllTableColumns().get(tableEntity.getAllEntityColumns().indexOf(column)))
		  .append(" ");
		
		return sb;
	} 
	private void setSimpleOpreation(boolean ifNeed,String column, Object value,String opreation) {
		if (ifNeed) {
			StringBuffer sb = getPublicSegment(column); 
			if (sb != null) { 
				String key = this.parameterPrefixName + column;
				sb.append(opreation)
				  .append(" ")
				  .append("#{parameter.")
				  .append(key)
				  .append("}");
				parameter.put(key, value);
				segmentSql.add(sb.toString());
			}
		}  
	}
	public WhereCustomSegment like(boolean ifNeed, String column, String value) {
		this.setSimpleOpreation(ifNeed, column, value + "%", "LIKE");
		return this;
	}
	public WhereCustomSegment or() {
		if (!this.currentOpreatioalType.equals("OR"))
			this.currentOpreatioalType = "OR";
		return this;
	}
	public WhereCustomSegment and() {
		if (!this.currentOpreatioalType.equals("AND"))
			this.currentOpreatioalType = "AND";
		return this;
	}
	public WhereCustomSegment between(boolean ifNeed, String column, Object left, Object right) {
		if (ifNeed) {
			StringBuffer sb = getPublicSegment(column); 
			if (sb != null) { 
				String key = this.parameterPrefixName + column;
				sb.append("BETWEEN #{parameter.").append(key).append("_left} AND #{parameter.").append(key).append("_right}");
				parameter.put(key + "_left",  left);
				parameter.put(key + "_right",  right);
				segmentSql.add(sb.toString());
			}
		}
		return this;
	}
	private Integer lastDivisionType() {
		int  i = segmentSql.size() - 1;
		for ( ; i >= 0; i --) {
			//has left '(' no  ')' 
			if (segmentSql.get(i).indexOf("(") != -1 && segmentSql.get(i).indexOf(")") == -1) {
				return leftDivision;
			}
		} 
		return rightDivision;
	}
	public WhereCustomSegment division() {
		
		if (this.lastDivisionType() == rightDivision) {
			segmentSql.add(" " + currentOpreatioalType + " (");
		} else {
			segmentSql.add(") " + currentOpreatioalType + " (");
		}
		return this;
	}
	public WhereCustomSegment between(String column, Object left, Object right) {
		return this.between(true, column, left, right);
	}
	public WhereCustomSegment like(String column, String value) { 
		return this.like(true, column, value);
	}
	public WhereCustomSegment notLike(boolean ifNeed, String column, String value) {
		this.setSimpleOpreation(ifNeed, column, value + "%", "NOT LIKE");
		return this;
	}
	public WhereCustomSegment notLike(String column,String value) { 
		return this.notLike(true, column, value);
	}
 
	public WhereCustomSegment eq(boolean ifNeed, String column, Object value) {
		this.setSimpleOpreation(ifNeed, column, value, "="); 
		return this;
	} 
	public WhereCustomSegment le(boolean ifNeed, String column, Object value) {
		this.setSimpleOpreation(ifNeed, column, value, "<="); 
		return this;
	} 
	public WhereCustomSegment le(String column, Object value) {
		this.le(true, column, value);
		return this;
	}
	public WhereCustomSegment lt(boolean ifNeed, String column, Object value) {
		this.setSimpleOpreation(ifNeed, column, value, "<"); 
		return this;
	} 
	public WhereCustomSegment lt(String column, Object value) {
		this.lt(true, column, value);
		return this;
	}
	public WhereCustomSegment ge(boolean ifNeed, String column, Object value) {
		this.setSimpleOpreation(ifNeed, column, value, ">="); 
		return this;
	}
	public WhereCustomSegment ge(String column, Object value) {
		this.ge(true, column, value);
		return this;
	}
	public WhereCustomSegment gt(boolean ifNeed, String column, Object value) {
		this.setSimpleOpreation(ifNeed, column, value, ">"); 
		return this;
	}
	public WhereCustomSegment gt(String column, Object value) {
		this.gt(true, column, value);
		return this;
	}
	public WhereCustomSegment eq(String column, Object value) {
		return this.eq(true, column, value);
	}
	public WhereCustomSegment notEq(boolean ifNeed, String column, Object value) {
		this.setSimpleOpreation(ifNeed, column, value, "<>"); 
		return this;
	}
	public WhereCustomSegment notEq(String column, Object value) {
		return this.notEq(true, column, value);
	}
	private void inOrNotIn(StringBuffer sb, String op, String column, Collection<?> value) {
		sb.append(op).append(" ("); 
		Iterator<?> iterator = value.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			
			StringBuffer sub = new StringBuffer();
			sub.append(parameterPrefixName)
			   .append(column)
			   .append("_")
			   .append(op.replaceAll(" ", ""))
			   .append("_")
			   .append(index ++);
			
			sb.append("#{parameter.")
			  .append(sub)
			  .append("},");
			
			this.parameter.put(sub.toString(), iterator.next());
		}
		//delete spare character ','
		sb.deleteCharAt(sb.length() - 1);  
		
		sb.append(")");
	}
	public WhereCustomSegment in(String column, Collection<?> value) { 
		return this.in(true, column, value);
	} 
	public WhereCustomSegment in(boolean ifNeed, String column, Collection<?> value) { 
		if (ifNeed) {
			StringBuffer sb = this.getPublicSegment(column);
			if (sb != null) {
				inOrNotIn(sb, "IN", column, value);
				segmentSql.add(sb.toString());
			}
		}
		return this;
	} 
	public WhereCustomSegment in(String column, Object[] value) { 
		return this.in(true, column, Arrays.asList(value));
	} 
	public WhereCustomSegment in(boolean ifNeed, String column, Object[] value) { 
		return this.in(ifNeed, column, Arrays.asList(value));
	} 
	public WhereCustomSegment notIn(String column, Collection<?> value) { 
		return this.notIn(true, column, value);
	} 
	public WhereCustomSegment notIn(boolean ifNeed, String column, Collection<?> value) { 
		if (ifNeed) {
			StringBuffer sb = this.getPublicSegment(column);
			if (sb != null) {
				inOrNotIn(sb, "NOT IN", column, value);
				segmentSql.add(sb.toString());
			}
		}
		return this;
	} 
	public WhereCustomSegment notIn(String column, Object[] value) { 
		return this.notIn(true, column, Arrays.asList(value));
	} 
	public WhereCustomSegment notIn(boolean ifNeed, String column, Object[] value) { 
		return this.notIn(ifNeed, column, Arrays.asList(value));
	} 
	public List<String> getSegmentSql() {
		if (this.lastDivisionType() == leftDivision) {
			segmentSql.add(")");
		}
		return segmentSql;
	}
	public void setSegmentSql(List<String> segmentSql) {
		this.segmentSql = segmentSql;
	}
	public Map<String, Object> getParameter() {
		return parameter;
	}
	public void setParameter(Map<String, Object> parameter) {
		this.parameter = parameter;
	}
}
