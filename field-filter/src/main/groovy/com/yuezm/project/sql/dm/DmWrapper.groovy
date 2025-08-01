package com.yuezm.project.sql.dm

import com.yuezm.project.sql.Wrapper


/**
 * DmWrapper
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/1 10:06
 */
class DmWrapper extends Wrapper {
    @Override
    String getColumn(String column) {
        return "\"${column}\""
    }
}
