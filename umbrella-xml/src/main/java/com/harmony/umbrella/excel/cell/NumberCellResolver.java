/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.harmony.umbrella.excel.cell;

import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Cell;

import com.harmony.umbrella.excel.ExcelUtil;

/**
 * @author wuxii@foxmail.com
 */
public class NumberCellResolver extends AbstractCellResolver<Number> {

    public static final NumberCellResolver INSTANCE = new NumberCellResolver();

    @Override
    public boolean isTargetType(Class<?> targetType) {
        return targetType.isAssignableFrom(Number.class);
    }

    @Override
    public Number resolve(int rowIndex, int columnIndex, Cell cell) {
        return new BigDecimal(ExcelUtil.getStringCellValue(cell));
    }

}