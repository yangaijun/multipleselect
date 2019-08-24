package com.freedomen.multipleselect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap; 
import java.util.List;
import java.util.Map;

import com.yaj.hyj.business.user.entity.po.UserPO;
import com.yaj.hyj.business.useraddress.entity.po.UserAddressPO;

public class MultipleSelect {
	
	//select all columns what you want
	private String columns;
	//the from table name
	private String masterTable;
	// order by columns
	private String orderBy;
	//left join table names 
	private List<String> join;
	//where sql segment
	private String sqlSegment;
	//TableEntity array
	private TableEntity[] tes;
	//user custom where sql segments
	private List<WhereCustomSegment> whereCustomSegments = null;
	//avoid sql injection parameters
	private Map<String, Object> parameter = null;
	
	private boolean addCustomFlag = true;
	
	private Integer start = null;

	private Integer end = null;
	
	public static MultipleSelect newInstance(String otherColumns, Collection<?> entities) {
		return  MultipleFactory.makeSelect(otherColumns, entities);
	}
	public static MultipleSelect newInstance(String otherColumns, Object ...entities) {
		return  MultipleFactory.makeSelect(otherColumns, entities);
	}
	
	public Map<String, Object> getParameter() {
		return parameter;
	}
	public void setParameter(Map<String, Object> parameter) {
		this.parameter = parameter;
	}
	public void addParameter(Map<String, Object> parameter) {
		if (this.parameter == null) {
			this.parameter = new HashMap<>();
		}
		this.parameter.putAll(parameter);
	}
	public String getColumns() {
		return columns;
	}
	public void setColumns(String columns) {
		this.columns = columns;
	}
	public String getMasterTable() {
		return masterTable;
	}
	public void setMasterTable(String masterTable) {
		this.masterTable = masterTable;
	} 
	
	public List<String> getJoin() {
		return join;
	}
	public void setJoin(List<String> join) {
		this.join = join;
	}
	public MultipleSelect clearJoin() {
		this.join.clear();
		return this;
	}
	//TODO left : ${0}.companyId, right : ${3}.companyId
	public MultipleSelect addJoin(String left, String right) {
		getTNCname(left);
		return this;
	}
	//TODO
	//get table,nick, column name by custom grammar ${0}.columnName
	private void getTNCname(String batch) {
		this.join.add("question.question_id = answer.question_id");
	}
	public String getSqlSegment() {
		//reject myBatis visit three times
		if (addCustomFlag) {
			addCustomFlag = false;
			this.setCustomWhere();
		}
		return sqlSegment;
	}
	public void setSqlSegment(String sqlSegment) {
		this.sqlSegment = sqlSegment;
	}
	private void setCustomWhere() {
		
		if (whereCustomSegments != null) {
			StringBuffer sb = new StringBuffer(sqlSegment);
			for (WhereCustomSegment i : whereCustomSegments) {
				if (parameter != null && i.getParameter() != null)
					addParameter(i.getParameter());
				
				for (String segment : i.getSegmentSql()) {
					sb.append(" ").append(segment);
				}
			} 
			
			for (TableEntity te : this.tes) {
				String logic = te.getLogicDelete();
				if (logic != null)
					sb.append(" AND ")
					  .append(te.getNickName())
					  .append(".")
					  .append(logic)
					  .append(" = ")
					  .append("0");
			}
			sqlSegment = sb.toString(); 
		}
	}

	public String getOrderBy() {
		return orderBy;
	}
	//"${0}.orderBy asc", "${1}.cmss desc"
	public void setOrderBy(String ...columns) {
		
		StringBuilder sb = new StringBuilder(); 
		for (String column : columns) {
			String[] t$2 = column.split(" ");
			String ob = MultipleFactory.getOtherColumnName(t$2[0], tes);
			if (ob != null) {
				sb.append(ob.split(" ")[0]);
				if (t$2.length == 2) {
					sb.append(" ").append(t$2[1]);
				}
				sb.append(",");
			}
		}
		//delete last character ','
		if (sb.length() != 0)
			sb.deleteCharAt(sb.length() - 1); 
		
		this.orderBy = sb.toString(); 
	}
	public TableEntity[] getTes() {
		return tes;
	}
	public void setTes(TableEntity[] tes) {
		this.tes = tes;
	}
	public WhereCustomSegment where(String table) {
		
		String tableDeputyName = table.replaceAll("\\$\\{|}", "");
		TableEntity tableEntity = null;
		try {
			int tableIndex = Integer.parseInt(tableDeputyName);
			
			if (tableIndex < tes.length) 
				tableEntity = tes[tableIndex]; 
		
		} catch (Exception e) {  
			for (TableEntity te : tes) {
				String column = tableDeputyName.toLowerCase();
				if (column.equals(te.getNickName().toLowerCase()) || column.equals(te.getTableName().toLowerCase())) {
					tableEntity = te;
					break;
				}
			} 
		}
		
		if (tableEntity == null) {
			(new Exception("no table '" + table + "' be found,use default table : '0'")).printStackTrace();
			tableEntity = tes[0];
		}
		
		WhereCustomSegment whereCustomSegment = new WhereCustomSegment(tableEntity);
		if (whereCustomSegments == null) {
			whereCustomSegments = new ArrayList<>();
		}
		whereCustomSegments.add(whereCustomSegment);
		
		return whereCustomSegment; 
				
	}
	 
	public Integer getStart() {
		return start;
	}
 
	public Integer getEnd() {
		return end;
	}
 
	public void setPage(Integer pageNo, Integer pageSize) {
		
		if (pageSize == null || pageSize == 0 || pageNo == null) {
			start = null; end = null;
		} else {
			start = (pageNo - 1)* pageSize;
			end = pageSize;
		}
			
	} 
	
	
	public static void main(String[] args) {
		MultipleSelect ms = MultipleSelect.newInstance("${1}", new UserPO(), new UserAddressPO());
			ms.where("${1}")
				.in("userAddressId", Arrays.asList(1, 2, 3))
				.like("userAddressRegion", "123456")
				.between("createTime", new Date(), 45)
				.division()
				.eq("userAddressName", "cmad")
				.or()
				.ge("userAddressMaster", 0);
			ms.getSqlSegment();
			System.out.println(ms.getSqlSegment());
				
	}
}
