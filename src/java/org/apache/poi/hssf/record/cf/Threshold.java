/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.record.cf;

import java.util.Arrays;

import org.apache.poi.ss.formula.Formula;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.ConditionalFormattingThreshold.RangeType;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Threshold / value for changes in Conditional Formatting
 */
public final class Threshold {
    /**
     * Cell values that are equal to the threshold value do not pass the threshold
     */
    public static final byte EQUALS_EXCLUDE = 0;
    /**
     * Cell values that are equal to the threshold value pass the threshold.
     */
    public static final byte EQUALS_INCLUDE = 1;
    
    private byte type;
    private Formula formula;
    private Double value;
    private byte equals;

    public Threshold() {
        type = (byte)RangeType.NUMBER.id;
        formula = Formula.create(null);
        value = 0d;
    }

    /** Creates new Threshold */
    public Threshold(LittleEndianInput in) {
        type = in.readByte();
        short formulaLen = in.readShort();
        if (formulaLen > 0) {
            formula = Formula.read(formulaLen, in);
        } else {
            formula = Formula.create(null);
        }
        // Value is only there for non-formula, non min/max thresholds
        if (formulaLen == 0 && type != RangeType.MIN.id &&
                type != RangeType.MAX.id) {
            value = in.readDouble();
        }
        equals = in.readByte();
        // Reserved, 4 bytes, all 0
        in.readInt();
    }

    public byte getType() {
        return type;
    }
    public void setType(byte type) {
        this.type = type;
    }

    protected Formula getFormula() {
        return formula;
    }
    public Ptg[] getParsedExpression() {
        return formula.getTokens();
    }
    public void setParsedExpression(Ptg[] ptgs) {
        formula = Formula.create(ptgs);
    }

    public Double getValue() {
        return value;
    }
    public void setValue(Double value) {
        this.value = value;
    }
    
    public byte getEquals() {
        return equals;
    }
    public void setEquals(byte equals) {
        this.equals = equals;
    }

    public int getDataLength() {
        int len = 1 + formula.getEncodedSize();
        if (value != null) {
            len += 8;
        }
        len += 5;
        return len;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("    [CF Threshold]\n");
        buffer.append("          .type    = ").append(Integer.toHexString(type)).append("\n");
        buffer.append("          .formula = ").append(Arrays.toString(formula.getTokens())).append("\n");
        buffer.append("          .value   = ").append(value).append("\n");
        buffer.append("    [/CF Threshold]\n");
        return buffer.toString();
    }

    public Object clone() {
      Threshold rec = new Threshold();
      rec.type = type;
      rec.formula = formula;
      rec.value = value;
      rec.equals = equals;
      return rec;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeByte(type);
        if (formula == null) {
            out.writeShort(0);
        } else {
            formula.serialize(out);
        }
        if (value != null) {
            out.writeDouble(value);
        }
        out.writeByte(equals);
        out.writeInt(0); // Reserved
    }
}