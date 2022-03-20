package com.chainup.contract.bean

class CpTabInfo{
    var name: String
    var index = 0
    var extras: String? = null
    var extrasNum: Int? = null

    constructor(name: String, index: Int) {
        this.name = name
        this.index = index
    }

    constructor(name: String, extras: String?) {
        this.name = name
        this.extras = extras
    }
    constructor(name: String, index: Int, extras: String?) {
        this.name = name
        this.index = index
        this.extras = extras
    }
    constructor(name: String, index: Int, extras: Int?) {
        this.name = name
        this.index = index
        this.extrasNum = extras
    }

}