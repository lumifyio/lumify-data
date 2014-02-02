/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.xform;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BooleanMappedValueTransformerTest extends AbstractMappedValueTransformerTest<Boolean> {
    private static final String UNMAPPED_KEY = "unmapped";
    private static final Map<String, Boolean> TEST_MAP;
    static {
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        map.put("1", Boolean.TRUE);
        map.put("0", Boolean.FALSE);
        map.put("-1", null);
        TEST_MAP = Collections.unmodifiableMap(map);
    }

    @Override
    protected Map<String, Boolean> getTestMap() {
        return TEST_MAP;
    }

    @Override
    protected Boolean getNonNullDefault() {
        return Boolean.TRUE;
    }

    @Override
    protected String getUnmappedKey() {
        return UNMAPPED_KEY;
    }

    @Override
    protected AbstractMappedValueTransformer<Boolean> createXform(Map<String, Boolean> valueMap) {
        return new BooleanMappedValueTransformer(valueMap);
    }

    @Override
    protected AbstractMappedValueTransformer<Boolean> createXform(Map<String, Boolean> valueMap, Boolean defaultValue) {
        return new BooleanMappedValueTransformer(valueMap, defaultValue);
    }
}
