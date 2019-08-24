package com.freedomen.multipleselect.mapper;
 
import java.util.List;
import java.util.Map;

import com.freedomen.multipleselect.MultipleSelect;
 

public interface MultipleMapper{
    public List<Map<String, Object>> mulSelect(MultipleSelect param);
    public Integer countMulSelect(MultipleSelect param);
}
