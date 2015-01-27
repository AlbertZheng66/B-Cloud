
/**
 * 此文件包括了和表格相关的所有操作， 此文件依赖于 gt_base.js。
 * @autho albert
 * @since 0.1
 */

/**
 * 和表格相关的操作
 */
function GTable(tableDom) {

    // 表格唯一的编码
    this.__id = IdGenerator.getId();
	
    // 表格的dom 对象
    this.tableDom = _createJQuery(tableDom);

    // 表格配置
    var tableConfig;

    // 过滤、排序和翻页参数
    var fspParameter = new FspParameter();

    // 所有的行记录
    this.rows = new Array();

    // 列模型实例
    this.tableColumnModel = null;

    // 表选择模型实例
    this.selectionModel   = new SelectionModel();

    // 通用的行选择事件
    this.rowSelectionListener = new CommonRowSelectionListener(this.selectionModel);

    // 行监听器
    this.rowListeners     = [this.rowSelectionListener];
    
    this.decorate = function (_tableConfig, _rows) {
        if (_tableConfig == null) {
            // throw new Error("表格配置对象不能为空。");
            throw new Error("The instance of TableConfig can not be null.");
        }
        this.tableConfig = _tableConfig;
        if (_rows != null) {
            this.rows = _rows;
        } else {
            this.rows = [];
        }

        // 赋值
        this.rowRenderer = _tableConfig.rowRenderer || this.rowRenderer;
        if (_tableConfig.selectionMode != null) {
            this.setSelectionMode(_tableConfig.selectionMode);
        }
        

        //TODO: 清除已有数据（这些数据有可能是演示数据）
        var columnInfos = _tableConfig.getColumnInfos();
        this.tableColumnModel = new TableColumnModel(columnInfos);


        // 创建表格构造器实例
        this.tableBuilder = new TableBuilder(this, this.tableDom, _tableConfig, this.rows);
        this.tableBuilder.rowRenderer    = this.rowRenderer;
		this.selectionModel.mode         = _tableConfig.selectionMode;
        this.tableBuilder.selectionModel = this.selectionModel;
        this.tableBuilder.rowListeners   = this.rowListeners;
        this.tableBuilder.build();
    };

    /**
	* FIXME:
     * 加载指定的页数据。
     */
    this.turnToPage = function(pageNo) {
        var rowIndex = this.fspParameter.pagination.rowsPerPage * pageNo;
        this.fspParameter.setStartIndex(rowIndex);
	    var rows = this.tableConfig._loadData();
        this.reload(rows);
    };
	
	 /**
     * 加载指定的页数据。
	 *FIXME:
     */
    this.refresh = function() {
	    var rows = this.tableConfig._loadData();
        this.reload(rows);
    };

    /**
     * 返回当前表的绑定类。
     */
    this.getBindingClass = function () {
        if (this.tableConfig != null) {
            return this.tableConfig.className;
        }
        return null;
    };

    /** 在当前数据之后，追加行记录 */
    this.load = function(_rows) {
        if (_rows == null) {
            // throw new "parameter can not be null.";
            _rows = [];
        }
        if (!(_rows instanceof Array)) {
            throw new "parameter is not a Array.";
        }
        for(var i = 0; i < _rows.length; i++) {
            var row = _rows[i];
            if (row != null) {
                this.rows.push(row);
            }
        }
        this.tableBuilder.rows = this.rows;
        this.tableBuilder.buildBody();

    }

    /** 删除所有行之后，追加行记录 */
    this.reload = function (_rows) {
        this.clear();
        this.load(_rows);
    }

    /** 删除所有行 */
    this.clear = function () {
        this.rows = new Array();
        this.tableBuilder.deleteAll();
    }

    /** 在指定位置追加行记录 */
    this.insertAt = function(_row, rowIndex) {
        // 校验越界
        if (_row == null) {
            return;
        }
        this.rows.splice(rowIndex, 0, _row);
    };

    /** 在所有记录之后追加行记录 */
    this.appendRow = function(_row) {
        if (_row != null) {
            this.rows.push(_row);
            this.tableBuilder.appendRow(_row);
        }
    };

    /**
     * 校验行索引是否越界。
     */
    this.validateRowIndex = function (rowIndex) {
        if (rowIndex == null || isNaN(rowIndex)) {
            throw new Error("row index can not be null and must be a number.");
        }
        if (rowIndex < 0 || rowIndex >= this.rows.length) {
            throw "out of row index error";
        }
    }

    /** 删除所有选中的行 */
    this.getRowAt = function (rowIndex) {
        this.validateRowIndex(rowIndex);
        return this.rows[rowIndex];
    }

    /** 删除所有选中的行 */
    this.deleteRow = function (rowIndex) {
        this.validateRowIndex(rowIndex);
        this.rows.splice(rowIndex, 1);
        this.tableBuilder.deleteRow(rowIndex);
    }

    /** 更新所有选中的行 */
    this.updateRow = function (rowIndex, row) {
        this.validateRowIndex(rowIndex);
        this.rows.splice(rowIndex, 1, row);
        this.tableBuilder.updateRow(rowIndex, row);
    }

    /** 返回当前表的行数 */
    this.getRowCount = function () {
        return this.rows.length;
    }

    /** 返回当前表的列数 */
    this.getColumnCount = function() {
        return this.tableColumnModel.getColumnCount();
    }
    //
    /** 设置某个指定单元格的值 */
    this.setValueAt = function (rowIndex, colIndex, value) {
        var name = this.tableColumnModel.getPropertyName(colIndex);
        var row = this.getRowAt(rowIndex);
        row[name] = value;
    }

    /** 返回指定某个指定单元格的值 */
    this.getValueAt = function (rowIndex, colIndex) {
        var name = this.tableColumnModel.getPropertyName(colIndex);
        var row = this.getRowAt(rowIndex);
        return row[name];
    }

    /** 返回第一个读到的选中行,如果没有选中行，则返回 null。 */
    this.getSelectedRow = function () {
        var selectedRow = this.selectionModel.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        return this.getRowAt(selectedRow);
    }

    /** 返回第一个读到的选中行,如果没有选中行，则返回 null。 */
    this.getSelectedRows = function () {
        var selectedRowIndies = this.selectionModel.getSelectedRows();
        if (selectedRowIndies == null || selectedRowIndies.length ==0) {
            return null;
        }
        var selectedRows = [];
        for(var row = 0; row < selectedRowIndies.length; row++) {
            selectedRows.push(this.getRowAt(row));
        }
        return selectedRows;
    }

    /**
     * 返回第一个读到的选中行的行号,如果没有选中行，则返回 -1。
     *
     */
    this.getSelectedRowNum = function() {
        var selectedRow = this.selectionModel.getSelectedRow();
        if (selectedRow < 0) {
            return -1;
        }
        return selectedRow;
    }

    /**
     * 返回第一个读到的选中行的行号,如果没有选中行，则返回 -1。
     */
    this.getSelectedRowNums = function(){
         return this.selectionModel.getSelectedRows();
    }

    /**
     * 更新选中的行，如果选中多行，则选择第一个选中行（可能带有随机性），
     * 因此建议只在单选的情况下使用此方法。
     * @row 更新后的行，如果参数为空或者当前没有选中的行，则不进行任何处理。
     */
    this.updateSelectedRow = function (row){
        var selectedRow = this.getSelectedRow();
        if (row == null || selectedRow < 0) {
            return;
        }
        this.updateRow(selectedRow, row);
    }

    // setColumnRenderer(columnName, renderer) /** 设置列的呈现器 */

    /**
     * 选中指定的行
     */
    this.setSelectedRow = function (rowIndex) {
        this.validateRowIndex(rowIndex);
        this.selectionModel.setSelectedRow(rowIndex);
    }

    /**
     * 设置自定义的行呈现器。
     */
    this.setRowRenderer = function (rowRenderer) {
        this.rowRenderer = rowRenderer;
    }

    this.setSelectionMode = function (mode) {
        //TODO: 检验模式是否存在
        if (SelectionMode[mode] == null) {

        }
        if (mode == null) {
            mode = SelectionMode.SINGLE;
        }
        this.selectionModel.mode = mode;
    }

    /**
     * 监听选择的行是否发生了变化。注意：一个选择动作可能引起两个事件，一个是被选中行取消了选择状态；
     * 其二是未被选中的行被选中。
     */
    this.addRowSelectionListener = function (selectionListener) {
        if (selectionListener != null) {
            this.rowSelectionListener.addListener (selectionListener);
        }
    }

    /**
     * 监听行是否被点击的事件。
     */
    this.addRowClickedListener = function (rowClickedListener) {
        if (rowClickedListener != null) {
            this.rowListeners.push(rowClickedListener);
        }
    }

    /**
     * 监听行是否被双击的事件。
     */
    this.addRowDoubleClickListener = function (rowDoubleClickedListener) {

    }

}

/**
 * 通用的行选择监听器，通过此监听器将行点击事件转换为行选择事件。
 * @param selectionModel 选择模型
 */
function CommonRowSelectionListener (selectionModel) {
    // 行监听器唯一的编码
    this.__id = IdGenerator.getId();

    this.selectionModel = selectionModel;
    this.rowSelectionListeners = [];

    this.onClick = function (event) {
        var rowIndex  = event['rowIndex']; // 被选中的行号
        if (rowIndex == null) {
            // throw new Error("参数错误，行号不能为空。");
            throw new Error("The parameter[rowIndex] can not be null.");
        }

        if (event.selectionChanged) {
            var rse = new RowSelectionEvent(event.source, rowIndex);
            this.fire(rse);
        }
    }
}


jQuery.extend(CommonRowSelectionListener.prototype, Observable.prototype);

function RowSelectionEvent (source, rowIndex) {
    this.source = source;
    this.name = 'RowSelection';
    this.rowIndex = rowIndex;  // 被选中的行号
}

function TableEvent (tableId) {
    this.tableId = tableId;

    /**
     * 事件分派处理函数。每次点击表时，都将通过此分发器进行处理
     */
    this.eventDispatch = function (source) {

        jQuery.each(this.listeners, function(i, listener) {
            var method = source.name;
            if (listener[method] != null) {
                listener[method](source);
            }
        });
    }
}

// 实现 GTable 的事件监听接口
jQuery.extend(TableEvent.prototype, Observable.prototype);


/**
 * 表格的构建函数，用此函数可构造表格对象
 * @tableDom 表格的 DOM 对象，不能为空。
 * @columns  表格的列信息，可为空。
 * @rows      行数据，可为空。
 */
function TableBuilder (gTable, tableDom, tableConfig, rows) {
    this.gTable       = gTable;
    this.tableDom     = tableDom;
    this.tableConfig  = tableConfig;
    this.columnInfos  = tableConfig.getColumnInfos();
    this.rows         = rows;
    this.showHeader   = tableConfig.showHeader;  // 是否显示头信息
    this.rowRenderer    = null;     // 行呈现器（用于编辑整行记录）
    this.headerRenderer = null;   //TODO: 头呈现器
    this.selectionModel = new SelectionModel();  // 选择模型
    this.rowProcessor   = null;   // 行回调函数，在行构建之后调用此函数
    this.rowListeners   = [];     // 行监听器
    this.rowIds         = [];     // 所有的行对应的ID
    this.headerAttrs    = tableConfig.headerAttrs;     // 表头属性
    this.trAttrs        = {};      // 数据行的属性
    // 选择头的属性
    this.selectionHeaderAttrs = tableConfig.selectionHeaderAttrs;
    this.rowOverListeners   = [];  // 行监听器
    this.rowOverListeners.push(new RowOverProcessor(tableDom, tableConfig, tableConfig.shortCutConfig));
    // this.tdAttrs        =

    /**
     * 定义表格的样式
     */
    this.styles = {'oddRow' : 'tableOldRow',
                   'evenRow' : 'tableEvenRow',
                   'rollingRow' : 'tableRollingRow'};
}

/**
 * TODO: 对数据进行操作时要触发行选择事件。
 */
jQuery.extend(TableBuilder.prototype, {

    // rowSelectorPrefix : '_selector_',  // 行选择控件（复选框或者单选框的前缀）

    build : function () {
        // 构建之前删除所有数据
        this.tableDom.empty();

        if (this.showHeader) {
            this.buildHeaders(this.columnInfos);
        }
        this.buildBody();
    },

    /**
     * 构建列头信息
     */
    buildHeaders : function (columnInfos) {
        if (columnInfos == null || !(columnInfos instanceof Array)
            || columnInfos.length == 0) {
            return;
        }       

       // 构建thead
       var thead = jQuery("<thead></thead>").appendTo(this.tableDom);
       var th = jQuery("<tr></tr>").appendTo(thead);

       if (this.headerAttrs != null) {
           jQuery.each(this.headerAttrs, function (name, value) {
               th.attr(name, value);
           });
       }

       // 构建选择行
       if (this.isCreateSelectionHeader()) {
           var selectionHeader = jQuery("<td> --*-- </td>");
           if (this.selectionHeaderAttrs != null) {
               jQuery.each(this.selectionHeaderAttrs, function (name, value) {
                   selectionHeader.attr(name, value);
               });
           }

            th.append(selectionHeader);
        }

        // 创建头呈现器(HeaderRenderer)
        for (var i = 0; i < columnInfos.length; i++) {
            var tableColumnInfo = columnInfos[i];
            var td = jQuery("<td>" + tableColumnInfo.title + "</td>").attr("name", tableColumnInfo.propertyName);

            // 读取属性信息
            td.attr('style', tableColumnInfo['style']);
            td.attr('className', tableColumnInfo['className']);
            if (this.headerRenderer != null) {
                var renderer = this.headerRenderer.getRenderer(td[0], columnInfos);
                if (renderer != null) {
                    td.val(renderer);
                }
            }
            th.append(td);
        }
    },


    /**
     * 判断是否构建选择头。其规则是选择模型不为空，且可以选择，且显示选择头，开始构建之。
     */
    isCreateSelectionHeader : function () {
        return (this.selectionModel != null && this.selectionModel.mode != SelectionMode.NONE
            && this.selectionModel.showSelectionHeader);
    },

    /**
     * 是否允许行选择（即触发行选择事件），选择模型不为空，且可以选择，返回 true。
     */
    isAllowRowSelection : function () {
        return (this.selectionModel != null
            && this.selectionModel.mode != SelectionMode.NONE);
    },

    /**
     * 构建表体信息。
     */
    buildBody : function() {
        var tbody = this.tableDom.children("tbody");
        if (tbody.length == 0) {
            tbody = jQuery("<tbody/>");
            this.tableDom.append(tbody);
        }
        for(var rowIndex = 0; rowIndex < this.rows.length; rowIndex++) {
            var row = this.rows[rowIndex];
            var tr = this.createRow(row, rowIndex);
            tbody.append(tr);
        }
    },

    /**
     * 创建一行记录
     */
    createRow : function (row, rowIndex) {
        var id = 'tr_' + IdGenerator.getId();  // 每行记录都有一个自动产生的ID
        this.rowIds.splice(rowIndex, 0, id);
        var tr = jQuery("<tr />").attr("id", id);

        // 设定奇数和偶数行
        if (!!this.tableConfig.showZebraBg) {
            var evenRow = SystemConfiguration.read('table.evenRow.className', 'evenRow');
            var oddRow  = SystemConfiguration.read('table.oddRow.className',  'oddRow');
            if ((rowIndex + 1) % 2 == 0) {
                tr.addClass(evenRow);
            } else {
                tr.addClass(oddRow);
            }
        }


        // 注册行选择事件
        if (this.isAllowRowSelection()) {
            tr.data('_rowListeners', this.rowListeners);
            tr.bind("click", {selectionModel : this.selectionModel, rowIds : this.rowIds},
                this.rowClicked);
        }

        // 注册鼠标进入和退出行事件
        if (true) {
            var params = {rowIndex : rowIndex, row : row, rowOverListeners : this.rowOverListeners};
            tr.bind("mouseout", params, this.mouseOut);
            tr.bind("mouseover", params, this.mouseOver);
        }

        // 构建选择行
        if (this.isCreateSelectionHeader()) {
            var _mode = this.selectionModel.mode;
			var rowHeader;
			if (_mode == SelectionMode.MULTI || _mode == SelectionMode.TOGGLE) {
                rowHeader = jQuery("<td> <input type='checkbox' id=" + '_selector_' + id + "  /> </td>");
			} else {
			    rowHeader = jQuery("<td> <input type='radio' id=" + '_selector_' + id + "  /> </td>");
			}
            tr.append(rowHeader);
        }
        if (this.rowRenderer != null) {
            // 自定义行构建器
            var _td = jQuery("<td> " + this.rowRenderer.getRow(row, rowIndex) + " </td>");
            tr.append(_td);
        } else {
            // 自动构建行
            for (var colIndex = 0; colIndex < this.columnInfos.length; colIndex++) {
                var tci = this.columnInfos[colIndex];
                var value =  row[tci.propertyName];
                var renderer = this._getRenderer(tci);
                var renderValue = value;
                if (renderer != null) {
                    renderValue = renderer.getRendererComponent(this.gTable,
                        value, rowIndex, colIndex);
                }
                var td = null;
                if (renderValue == null) {
                    td = jQuery("<td>&nbsp;</td>");
                } else if (typeof renderValue !== 'object') {
                    td = jQuery("<td>" + renderValue + "</td>");
                } else {
                    td = jQuery("<td></td>");
                    td.append(renderValue);
                }
                var dataColumnAttrs = tci['dataColumnAttrs'];
                // MessageBox.debug('dataColumnAttrs', dataColumnAttrs);
                if (dataColumnAttrs != null) {
                    jQuery.each(dataColumnAttrs, function (name, value) {
                        td.attr(name, value);
                    });
                }
                tr.append(td);
            }
        }

        // 处理行回调
        if (this.rowProcessor != null) {
            this.rowProcessor.process(tr, row, rowIndex);
        }

        return tr;
    },

    /**
     * 取得列的呈现器实例
     */
    _getRenderer : function (tci) {
        var renderer = null;
        var customizedRenderers = this.tableConfig.renderers; // 自定义的呈现器
        if (customizedRenderers != null && customizedRenderers[tci.propertyName] != null) {
            renderer = customizedRenderers[tci.propertyName];
        } else {
            renderer = CellRendererFactory.getCellRenderer(tci)
        }
        return renderer;
    },

    /**
     * 在行尾追加一行记录
     */
    appendRow : function (row) {
        if (row != null) {
            var rowIndex = this.getRowCount() - 1;  // 注意，数据已经增加到模型之中
            this.insertRowAt(rowIndex, row);
        }
    },

    /**
     * 返回当前的行数
     */
    getRowCount : function () {
        return this.rows == null ? 0 : this.rows.length;
    },

    /**
     * 更新指定的行记录数据信息
     */
    updateRow : function (rowIndex, row) {
        this.deleteRow(rowIndex);
        this.insertRowAt(rowIndex, row, true);
        // 重新绘制被选中的行
        TableBuilderHelper.paintSelector(this.rowIds, this.selectionModel);
    },

    /**
     * 在指定的位置插入一条行数据。
     */
    insertRowAt : function (rowIndex, row, autoSelection) {
        var tr = this.createRow(row, rowIndex);
        var tbody = this.tableDom.children("tbody");
        if (rowIndex == 0) {
            // TODO: 如果Body为空，自动创建一个
            // 在第一行追加记录
           tbody.prepend(tr);
        } else if (rowIndex >= tbody.children("tr").length) {
           tbody.append(tr);
        } else {
           var prevRow = tbody.children("tr")[rowIndex - 1];
           tr.insertAfter(jQuery(prevRow));
        }

        if (autoSelection == null || autoSelection) {
            this.selectionModel.setSelectedRow(rowIndex);

            // 重新绘制被选中的行
            TableBuilderHelper.paintSelector(this.rowIds, this.selectionModel);
        }
    },

    /**
     * 删除指定的行记录数据信息。
     */
    deleteRow : function (rowIndex) {
         var tr = this.tableDom.children("tbody").children("tr")[rowIndex];
         jQuery(tr).remove();
         // 删除ID的索引及选择模型的数据
         this.rowIds.splice(rowIndex, 1);
         this.selectionModel.deselectRow(rowIndex);

         // TODO: 重新绘制奇数和偶数行

    },

    /**
     * 删除所有的行记录数据。
     */
    deleteAll : function () {
        var tbody = this.tableDom.children("tbody");
        if (tbody.length > 0) {
            tbody.children('tr').remove();
        }
        this.selectionModel.deselectAll();
        this.rowIds = [];

    },

    /**
     * 此方法用来处理行点击事件
     */
    rowClicked : function (event) {
        var rowListeners =  jQuery(this).data('_rowListeners');
        // MessageBox.debug('rowListeners=', rowListeners);
        var tr = jQuery(this)[0];

        var rowIds = event.data.rowIds;
        var rowIndex = rowIds.indexOf(tr.id);
        //alert('rowIndex=' + rowIndex);
        var selectionModel  = event.data.selectionModel;

        // TODO: 表格变化前的事件处理

        var prevSelected     = selectionModel.isRowSelected(rowIndex);  // 之前是否被选中
        var selectionChanged = selectionModel.setSelectedRow(rowIndex) != prevSelected;

         // 重新绘制被选中的行
        TableBuilderHelper.paintSelector(rowIds, selectionModel);

        jQuery.each(rowListeners, function(i, rowListener) {
            var rcEvent = new RowClickEvent(tr);
            rcEvent.selectionChanged = selectionChanged;
            rcEvent['rowIndex'] = rowIndex; // 被选择的行
            rcEvent['mouseEvent'] = event;  // 鼠标的原始事件
            rowListener['onClick'](rcEvent);
        });
    },
    
    /**
     * 此方法用来处理行点击事件
     */
    mouseOver : function (event) {
        var tr = this;
        var rowIndex = event.data.rowIndex;
        var row      = event.data.row;
        var rowOverListeners =  event.data.rowOverListeners;
        jQuery.each(rowOverListeners, function(i, rowOverListener) {
            var roEvent = new RowOverEvent(tr);
            roEvent['rowIndex']   = rowIndex; // 被选择的行位置
            roEvent['row']        = row;      // 被选择的行索引
            roEvent['mouseEvent'] = event;  // 鼠标的原始事件
            if (rowOverListener['onMouseOver'] == null) {
                throw new Error('rowOverListener must have function which named onMouseOver.');
            }
            rowOverListener.onMouseOver(roEvent);
        });        
    },

    /**
     * 此方法用来处理行点击事件
     */
    mouseOut : function (event) {
        var tr = this;
        var rowIndex = event.data.rowIndex;
        var row      = event.data.row;
        var rowOverListeners =  event.data.rowOverListeners;
        jQuery.each(rowOverListeners, function(i, rowOverListener) {
            var roEvent = new RowOverEvent(tr);
            roEvent['rowIndex'] = rowIndex; // 被选择的行位置
            roEvent['row']      = row;      // 被选择的行索引
            roEvent['mouseEvent'] = event;  // 鼠标的原始事件
            if (rowOverListener['onMouseOut'] == null) {
                throw new Error('rowOverListener must have function which named onMouseOut.');
            }
            rowOverListener['onMouseOut'](roEvent);
        });
    }
});

/**
 * 绘制行前端的选择器
 */
var TableBuilderHelper = {
     paintSelector : function (rowIds, selectionModel) {
        for (var _rowIndex = 0; _rowIndex < rowIds.length; _rowIndex++) {
            var rowId = rowIds[_rowIndex];
            var inputId = '_selector_' + rowId;
            var input = jQuery("#" + inputId);
            // 未发现此对象，
            if (input.length == 0) {
                // throw new Error("未发现ID为[" + inputId + "]的对象。");
                throw new Error ("The instance which's ID named [" + inputId + "] can not be found.");
            }
            if (selectionModel.isRowSelected(_rowIndex)) {
                input.attr('checked', 'checked');
            } else {
                input.removeAttr('checked');
            }
        }
    }
}

/**
 * 行点击事件，每次行点击都会触发此事件。此事件继承自“Event”对象
 * @param source 行（“tr”）实例
 */
function RowClickEvent(source) {
    this.source   = source;   // 行（“tr”）实例
    this.name     = 'Click';  // 时间的名称
    this.rowIndex = -1;  // 被点击的行号
}
/**
 *
 */
jQuery.extend(RowClickEvent.prototype, Event.prototype);

/**
 * 当鼠标滑过行时产生的事件。此事件继承自“Event”对象
 * @param source 行（“tr”）实例
 */
function RowOverEvent(source) {
    this.source   = source;     // 行（“tr”）实例
    this.name     = 'RowOver';  // 时间的名称
    this.rowIndex = -1;         // 当前经过的行号
}


/**
 * 默认的行处理函数
 */
function RowOverProcessor (tableDom, tableConfig, shortCutConfig) {
    this.tableDom = jQuery(tableDom);
    this.rowIndex = -1;
    this.shortCutConfig = shortCutConfig;
    this.prevSource = null;
    this.fixedLeft  = -1;   // 用户拖动后的最后位置
    this.rowBgColor = null;  // 备份的行背景色
    this.tableConfig = tableConfig;
    this.rolloverBgcolor = tableConfig['rolloverBgcolor'];  // 鼠标滑过时的行背景色

    /**
     * 用于隔离用户的直接操作，统一设置数据
     */
    this.clickHandle = function (event) {
        var rowOverProcessor = event.data.rowOverProcessor;
        var action           = event.data.action;

        // 用户自定义的动作
        action(rowOverProcessor.roEvent);
    }

    /**
     * 构建用户的快捷方式，程序员可以定义快捷菜单的样式，参数名称为：
     */
    this.createShortCuts = function (_shortCuts) {
        // shortCuts 必须是数组
        if (_shortCuts == null || _shortCuts.length == 0) {
            return null;
        }
        var shortCutsComp = jQuery("<div id='_shortCuts_' /></div>");
        
        for(var i = 0; i < _shortCuts.length; i++) {
            var sc = _shortCuts[i];
            var btn = jQuery("<img id='_shortCuts_" + IdGenerator.getId() + "'></img>");
            btn.attr({src: sc.icon, alt: sc.name, title : sc.name, height : 20, width:20});
            if (sc.action == null) {
                // throw new Error('快捷键的动作不能为空。');
                throw new Error('The action[' + sc.name + '] of short cut can not be null');
            }
            btn.bind('click', {rowOverProcessor : this, action : sc.action}, this.clickHandle);
            shortCutsComp.append(btn);
        }
        shortCutsComp.css({position: 'absolute'});

        if (this.shortCutConfig.className != null) {
            shortCutsComp.addClass(this.shortCutConfig.className);
        } else {
            // 标准的默认的标识
            shortCutsComp.css({
                background : '#ff0000',  //'#e9eff8',
                //opacity : 0.5,
                //width  : 100,
                height : 20,
                display : 'none',
                'z-index':100
            });
        }

        //MessageBox.debug('html=', shortCutsComp.text());

        shortCutsComp.insertAfter(this.tableDom);

        var _rowOverProcessor = this;
        shortCutsComp.draggable({ stop: function(event, ui) {
                // 记录最后固定的值
                _rowOverProcessor.fixedLeft = event.clientX;
            }
        });
        return shortCutsComp;
    }

    // MessageBox.debug('shortCutConfig=', shortCutConfig);
    // 开始构建快捷方式
    if (shortCutConfig != null) {
        this.shortCuts = this.createShortCuts(shortCutConfig.shortCuts);
    }    

    this.onMouseOut = function (roEvent) {
        if (this.shortCuts == null) {
            return;
        }
        var mouseEvent = roEvent['mouseEvent'];  // 鼠标的原始事件

        // 鼠标在快捷菜单区域内
        if (Utils.isMouseOver(mouseEvent, this.shortCuts)) {
                return;
        }

        // 鼠标仍然在此行内
        if (Utils.isMouseOver(mouseEvent, this.prevSource)) {
            return;
        }
        
        this.rowIndex = -1;
        // MessageBox.debug('onMouseOut posi', posi);
        this.shortCuts.css('display', 'none');

        // 设置原有的背景色
        if (this.rolloverBgcolor) {
            jQuery(this.prevSource).css('background-color', this.rowBgColor);
        }
    }
    
    this.onMouseOver = function (roEvent) {
        if (this.shortCuts == null) {
            return;
        }
        
        // 避免重复操作
        var _rowIndex = roEvent.rowIndex;
        // MessageBox.debug('onMouseOver _rowIndex' + _rowIndex + "; this.rowIndex=" + this.rowIndex);
        if (_rowIndex == this.rowIndex) {
            return;
        }

        // 默认将显示条显示在列表的左侧
        // TODO: 以后可以设置显示模式
        if(this.fixedLeft < 0) {
            this.fixedLeft = this.tableDom.position().left
                + this.tableDom.width() - this.shortCuts.width() - 10;  // 10 为右侧的补充空间
        } else {
            // 快捷菜单在鼠标的右侧显示
            // this.fixedLeft > -1 ? this.fixedLeft : mouseEvent.clientX + 100
        }

        this.roEvent  = roEvent;
        this.rowIndex = _rowIndex;
        var source = roEvent.source;
        this.prevSource = source;
        var _source = jQuery(source);
        var posi = _source.position();
        this.shortCuts.css('display', 'block');
        this.shortCuts.css('top', posi.top + 3);
        this.shortCuts.css('left', this.fixedLeft);  // 快捷菜单距离鼠标的位置
        
       this.shortCuts.draggable('option', 'containment', [posi.left, posi.top,
                posi.left + _source.width() - this.shortCuts.width(), 
                posi.top + _source.height() - this.shortCuts.height()]);

        // 设置箭头滑过时的行的颜色
        if (this.rolloverBgcolor) {
            this.rowBgColor = _source.css('background-color');
            _source.css('background-color', this.rolloverBgcolor);
        }
    }
}

/**
 * 定义快捷操作菜单的类
 */
function ShortCut (name, icon, action) {
    this.name   = name;    // 动作
    this.action = action;  // 执行的方法
    this.icon   = icon;    // 动作的图标
}

/**
 *
 */
jQuery.extend(RowOverEvent.prototype, Event.prototype);


function TableColumnModel (columnInfos) {
    /**
     * 可见列集合
     */
    this.visibleColumnInfos = columnInfos;

    //
    // this.showColumn(propertyName);
    // this.hideColumn(propertyName);
    // this.setColumnRenderer();
    // this.isEditable()
    this.getColumnCount = function () {
        var count = 0;
        if (this.visibleColumnInfos != null) {
            count = this.visibleColumnInfos.length;
        }
        return count;
    }

    this.validateColIndex = function(colIndex) {
        if (colIndex < 0 || colIndex >= this.getColumnCount()) {
            throw "out of column index";
        }
        return true;
    }

    this.getColumnInfos = function() {
        return this.visibleColumnInfos;
    }

    this.getColumnInfo = function (colIndex) {
        this.validateColIndex(colIndex);
        return this.visibleColumnInfos[colIndex];
    }

    this.getPropertyName = function (colIndex) {
        var columnInfo = this.getColumnInfo(colIndex);
        return columnInfo["propertyName"];
    }
}

function TableEvent () {

}

function TableSelectionEvent() {

}

/**
 * 表的选择模式，包括：单选（默认方式），多选，使用开关选择模式（多选），不可选择
 */
var SelectionMode = {
    SINGLE : 'SINGLE',  // 单选（默认方式）
    MULTI  : 'MULTI',   // 多选
    //TOGGLE : 'TOGGLE',  // 使用开关选择模式（多选）
    NONE   : 'NONE'     // 不可选择
}

/**
 * 表的选择模型，记录了表已经被选择的行，表的选择模式的等信息
 */
function SelectionModel () {
    // 是否显示选择头，默认为显示
    this.showSelectionHeader = true;

    // 是否允许全选、全不选

    // 选择模式
    this.mode = SelectionMode.SINGLE;

    // 已经被选中的行
    this.selectedIndices = [];
}

SelectionModel.prototype = {

    /**
     * 设置选中的行，当用户点击(选择)了表格的行时，或者调用此方法设置行时，即设定为此行。
     * 具体此行最终是否被选中，需要根据其“模式”进行处理。如果是单选和多选，则此行必备选中；
     * 如果是“开关”模式，且当前未必选中，则将此行设置为选中状态。其他情况，此行设置为未选中。
     *
     */
    setSelectedRow : function (rowNum) {
        var selected = false;
        if (this.mode == SelectionMode.SINGLE) {
             this.selectedIndices = [];
             this.selectedIndices.push(rowNum);
             selected = true;
         } else if (this.mode == SelectionMode.MULTI) {
        //     if (!this.isRowSelected(rowNum)) {
        //         this.selectedIndices.push(rowNum);
        //         selected = true;
        //     }
        // } else if (this.mode == SelectionMode.TOGGLE) {
             if (this.isRowSelected(rowNum)) {
                    this.deselectRow(rowNum);
             } else {
                 this.selectedIndices.push(rowNum);
                 selected = true;
             }
        } else {
            this.selectedIndices = [];
        }
        return selected;
    },

    /**
     * 返回选中的行，如果是多选，则只返回第一行。如果有选中的行，返回行号，否则返回 “-1”。
     * @return 选中的行号或者“-1”。
     */
    getSelectedRow : function () {
       if (this.selectedIndices.length == 0) {
           return -1;
       } else {
           return this.selectedIndices[0];
       }
    },

    /**
     * 返回所有选中的行。
     */
    getSelectedRows : function () {
       return this.selectedIndices;
    },

    /**
     * 判断指定的行是否被选中。
     */
    isRowSelected : function (rowNum) {
        for(var i = 0; i < this.selectedIndices.length; i++) {
            if (rowNum == this.selectedIndices[i]) {
                return true;
            }
        }
        return false;
    },

    /**
     * 取消所有选中行。
     */
    deselectRow : function (rowNum) {
        var _indices = [];
        for(var i = 0; i < this.selectedIndices.length; i++) {
            if (rowNum != this.selectedIndices[i]) {
                _indices.push(this.selectedIndices[i]);
            }
        }
        this.selectedIndices = _indices;
    },

    /**
     * 取消所有选中的行。
     */
    deselectAll : function () {
        this.selectedIndices = [];
    }
}

var TableConfig = function (className, full) {
    if (className == null || className == '') {
        // throw new Error("被绑定的类名称不能为空。");
        throw new Error ('The binding class can not be null.');
    }

    // 需要复制的属性的值
    this.ATTR_NAMES = ['style', 'class'];


    this.className = ClassLoader.getFullName(className, full);

    // 是否显示表头，默认为显示。
    this.showHeader = true;

    // 采用何种方式创建表头(根据标注信息进行创建，或者是从表格模板信息开始创建)
    this.headerCreator = 'anno';  //anno | template | auto

    // 列信息集合
    this.columnInfos = null;

    // 数据属性，这些值将输出到表体的tr中
    this.dataAttributes = {};

    // 删除后自动选择当前行的下一行数据
    this.autoSelectionAfterRowDeleted = true;

    // 增加后自动选择新增加的行
    this.autoSelectionAfterRowAdded = true;


    // TODO: 程序员可以自定义每页的行数, 如果此值小于等于0，表示采用系统定义
    this.rowsPerPage = -1;

    // 行呈现器
    this.rowRenderer   = null;

    // 选择模式，默认为单选
    this.selectionMode = SelectionMode.SINGLE;

    // 表头属性（这些值将输出到表头的tr中）
    this.headerAttrs    = {};

    // 选择头的属性（这些值将输出到选择类的标题行的td中）
    this.selectionHeaderAttrs = {};

    // 是否显示斑马条
    this.showZebraBg = true;

    // 快捷键相关的参数，包含的属性有：ShortCuts, show
    this.shortCutConfig  = {};
}

jQuery.extend(TableConfig.prototype, {

    // 返回所有的列信息
    getColumnInfos : function () {
        if (this.columnInfos != null) {
            return this.columnInfos;
        }
        this.columnInfos = this.createColumnsFromAnnotation();
        return this.columnInfos;
    },

	/**
	  * 从已有模板中构建列信息。
	  */
	createColumnsFromTemplate : function (tableDom) {
        this.headerCreator = 'template';  // 设定头的创建方式
        var headerRow = this.getHeaders(tableDom);
        
        // 读取表头的属性
        for (var i = 0; i < this.ATTR_NAMES.length; i++) {
            var attrName = this.ATTR_NAMES[i];
            this.headerAttrs[attrName] = jQuery(headerRow).attr(attrName);
        }

        // MessageBox.debug('this.headerAttrs=', this.headerAttrs);
        

        var tds = jQuery(headerRow).children('td');
        if (tds.length <= 0) {
            // throw new Error("行标签（tr）中未定义任何 td 标签。");
            throw new Error("There are not td tags in the tr tag.");
        }

        var className = this.className;
        var thisColumnInfos = [];

        // 从服务端读取列信息
        var req = new Request("com.xt.gt.html.service.TableService", "getColumnInfos", className);
        var _columnInfos = ServiceInvoker.invoke(req);
		if(_columnInfos == null || _columnInfos.length == 0) {
		    // throw new Error("类[" + className + "]未定义任何属性信息。");
            throw new Error("The class[" + className + "] does not define any properties.");
		}
        
        var columnMap = {};  // 主键是：名称，键值是columnInfo对象。
        jQuery.each(_columnInfos, function(j, columnInfo) {
            //alert("columnInfo['propertyName']=" + columnInfo['propertyName']);
            columnMap[columnInfo['propertyName']] = columnInfo;
        });

        // 取得模板列的属性
        var columnsAttrs = this.getRowTemplate(tableDom);
        
        var ATTR_NAMES = this.ATTR_NAMES;
        jQuery.each(tds, function(i, td) {
            var _td = jQuery(td);
            var name = _td.attr('name');
            if (name == null) {
                // throw new Error("头标签必须定义“name”属性。");
                throw new Error("The td tag must define the attribute named 'name'");
            }
            var _columnInfo = columnMap[name];
            if (_columnInfo == null) {
                // throw new Error("属性[" + name + "]在类[" + className + "]中不存在。");
                throw new Error("The attribute [" + name + "] does not exist in class[" + className + "].");
            }
            _columnInfo['title'] = _td.text();
            for (var _i = 0; _i < ATTR_NAMES.length; _i++) {
                var attrName = ATTR_NAMES[_i];
                _columnInfo[attrName] = _td.attr(attrName);
            }

            // 列的数据属性
            _columnInfo['dataColumnAttrs'] = columnsAttrs[i];
            // MessageBox.debug('_columnInfo[dataColumnAttrs]', _columnInfo['dataColumnAttrs']);

            thisColumnInfos.push(_columnInfo);
        });
        // MessageBox.debug('thisColumnInfos', thisColumnInfos);
        
        this.columnInfos = thisColumnInfos;
        return this.columnInfos;
    },

    /**
     * 通过标注创建列信息。
     */
    createColumnsFromAnnotation : function(includeColumns, excludeColumns) {
        this.headerCreator = 'anno';  // 设定头的创建方式

        var className = this.className;

        // 从服务端读取列信息
        var req = new Request("com.xt.gt.html.service.TableService", "getAnnotatedColumnInfos", className);
        var _columnInfos = ServiceInvoker.invoke(req);

        var thisColumnInfos = [];
        // 如果明确定义需要显示的列，则按照此顺序显示所有的列。注意：排除列不生效
        if (includeColumns != null) {
            var columnMap = {};  // 主键是：名称，键值是columnInfo对象。
            jQuery.each(_columnInfos, function(j, columnInfo) {
                //alert("columnInfo['propertyName']=" + columnInfo['propertyName']);
                columnMap[columnInfo['propertyName']] = columnInfo;
            });

            jQuery.each(includeColumns, function (i, columnName) {
                var _columnInfo = columnMap[columnName];
                if (_columnInfo == null) {
                    // alert("属性[" + columnName + "]在类[" + className + "]中不存在。");
                    // throw new Error("属性[" + columnName + "]在类[" + className + "]中不存在。");
                    throw new Error("The attribute [" + columnName + "] does not exist in class[" + className + "].");
                }
                thisColumnInfos.push(_columnInfo);
            });
        } else if (excludeColumns != null) {
            // 如果定义了排除列
            jQuery.each(_columnInfos, function(j, columnInfo) {
                if (jQuery.inArray(excludeColumns, columnInfo['propertyName']) < 0) {
                    thisColumnInfos.push(columnInfo);
                }
            });
        } else {
            thisColumnInfos = _columnInfos;
        }
        this.columnInfos = thisColumnInfos;
        return this.columnInfos;
    },


    /**
     * 在表模板的基础上构建表头。创建的规则是：
     * 1. 首先取头标签里的第一行“tr”；
     * 2. 如果未取到，然后尝试表标签里的第一行“tr”；
     * 3. 最后取尝试体（tbody）标签里的第一行“tr”。
     * 4. 上述几种方式都没取到头数据的情况下，则抛出异常。
     */
    getHeaders : function(tableDom) {
        if (tableDom == null) {
            // throw new Error('表模型不能为空。');
            throw new Error ('The paramter can not be null.');
        }
    	// 首先取头标签里的第一行“tr”
    	var headerRow = null;
    	if (jQuery(tableDom).children('thead').length > 0) {
	        var thead = jQuery(tableDom).children('thead')[0];
			if (jQuery(thead).children('tr').length == 0) {
			    // throw new Error("头标签(thead)里未定义行(tr)标签。");
                throw new Error("There are any tr tag in thead.");
			}
			headerRow = jQuery(thead).children('tr')[0];
		}

		// 然后尝试表标签里的第一行“tr”
    	headerRow = headerRow || jQuery(tableDom).children('tr').length > 0 ? jQuery(tableDom).children('tr')[0] : null;

		if (headerRow == null) {
        	// 最后取尝试体（tbody）标签里的第一行“tr”
			var tbody = jQuery(tableDom).children('tbody').length > 0 ? jQuery(tableDom).children('tbody')[0] : null;
			if (tbody != null) {
            	headerRow = jQuery(tbody).children('tr').length > 0 ? jQuery(tbody).children('tr')[0] : null;
			}
		}

        if (headerRow == null) {
        	// throw new Error("表里未包含行数据。");
            throw new Error("The table must contain header.");
    	}
        
		return headerRow;
    },

    /**
     * 在表模板的基础上构建行数据。查找规则是的规则是：
     * 1. 首先查找ID为“rowTemplate”的 tr，标签，然后读取其中的每一行；
     * 2. 如果未取到，然后尝试体（tbody）标签里的第一行“tr”;
     * 3. 最后尝试表标签里的第二行“tr”；。
     * 4. 上述几种方式都没取到头数据的情况下，则返回空。
     * @return 列属性的数组
     */
    getRowTemplate : function(tableDom) {
        if (tableDom == null) {
            // throw new Error('表模型不能为空。');
            throw new Error('The parameter can not be null.');
        }
    	// 首先查找名称为“rowTemplate”的 tr，标签
    	var templateRow = null;
        if (jQuery('#rowTemplate').length > 0) {
            templateRow = jQuery('#rowTemplate');
        }

        if (templateRow == null) {
        	// 最后取尝试体（tbody）标签里的第一行“tr”,如果没有“tbody”便签，则取第二行
			var tbody = jQuery(tableDom).children('tbody').length > 0 ? jQuery(tableDom).children('tbody') : null;
			if (tbody != null) {
            	templateRow = tbody.children('tr').length > 0 ? jQuery(tbody).children('tr')[0] : null;
			} else {
                templateRow = jQuery(tableDom).children('tr').length > 1 ? jQuery(tbody).children('tr')[1] : null;
            }
		}

        // 是否有头文件
        // var hasHead  = (jQuery(tableDom).children('thead').length > 0);
        var columnsAttrs = [];
        if (templateRow != null) {
            jQuery.each(jQuery(templateRow).children('td'), function (index, _td) {
                var attrs = {};
                attrs['style']     = jQuery(_td).attr('style');
                attrs['className'] = jQuery(_td).attr('class');
                columnsAttrs[index] = attrs;
            });
        }
        // MessageBox.debug('columnsAttrs', columnsAttrs);
		return columnsAttrs;
    }
});

/**
 * 用一个简化的方法创建一个"模板表"的配置文件
 */
var TemplateTableConfig = {
    /**
     * 创建一个默认的模板配置
     */
    create : function (className, full, process) {
         var tableConfig = new TableConfig(className, full);
         tableConfig.showHeader    = false;               // 不要显示表头
         tableConfig.selectionMode = SelectionMode.NONE;  // 不要行选择
         tableConfig.showZebraBg   = false;               // 不显示斑马条
         tableConfig.rowRenderer   = new TemplateRowRenderer('template', process); // 创建模板编辑器
         return tableConfig;
    }
}

function TableModel() {

}

var TableCellRenderer = function () {

    /**
     * 返回表格呈现器控件。
     */
    this.getRendererComponent = function(table, value, rowIndex, colIndex) {

    }
}

/**
 * 空数据的表示。
 */
TableCellRenderer.NULL_VALUE_TEXT = '&nbsp;';



function DefaultCellRenderer() {
    /**
     * 返回呈现器使用的控件
     */
}

/**
 * 返回呈现器使用的控件
 * @param table GTable 实例
 * @param value 当前表的单元格的实例
 * @param rowIndex 当前值所在的行
 * @param colIndex 当前值所在的列
 */
DefaultCellRenderer.prototype.getRendererComponent = function (table, value, rowIndex, colIndex) {
    var text = TableCellRenderer.NULL_VALUE_TEXT;
    if (value != null) {
        text += value;
    }
    return text;
}

function DateCellRenderer() {
    /**
     * 返回呈现器使用的控件
     */
}

/**
 * 返回呈现器使用的控件
 */
DateCellRenderer.prototype.getRendererComponent = function (table, value, rowIndex, colIndex) {
    var text = TableCellRenderer.NULL_VALUE_TEXT;
    if (value == null) {
        return text;
    }
    var date = null;
    if (!isNaN(value)) {
        date = new Date();
        date.setTime(value);
    } else if (value instanceof Date) {
        date = value;
    } 
    if (date != null) {
        var datePattern = SystemConfiguration.read('format.date', 'yyyy-MM-dd');
        text = DateFormat.format(date, datePattern);
    } else {
        text += value;
    }
    return text;
}

/**
 * 用于处理枚举类型的呈现器
 */
function EnumRenderer() {
}

/**
 * 返回呈现器使用的控件
 */
EnumRenderer.prototype.getRendererComponent = function (table, value, rowIndex, colIndex) {
    var text = TableCellRenderer.NULL_VALUE_TEXT;
    if (value == null) {
        return text;
    }
    text = value.title || value.name;
    return text;
}

/**
 * 整数呈现器
 */
function IntCellRenderer() {
    /**
     * 返回呈现器使用的控件
     */
}

/**
 * 返回呈现器使用的控件
 */
IntCellRenderer.prototype.getRendererComponent = function (table, value, rowIndex, colIndex) {
    var text = TableCellRenderer.NULL_VALUE_TEXT;
    if (value == null) {
        return text;
    }
    if (isNaN(value)) {
        throw new Error ("The value [" + value + "] must be a number.");
    }
    // 是否为自动转换
    // var _value = typeof value === 'string' ? parseInt(value) : value;
    var format = SystemConfiguration.read('format.int', '#,###')
    if (format != null && format != '') {
        text = NumberFormat.format(value, format);
    } else {
        text += value;
    }
    return text;
}

/**
 * 浮点数呈现器
 */
function DoubleCellRenderer() {
    /**
     * 返回呈现器使用的控件
     */
}

/**
 * 返回呈现器使用的控件
 */
DoubleCellRenderer.prototype.getRendererComponent = function (table, value, rowIndex, colIndex) {
    var text = TableCellRenderer.NULL_VALUE_TEXT;
    if (value == null) {
        return text;
    }
    if (isNaN(value)) {
        throw new Error ("The value [" + value + "] must be a number.");
    }
    // 是否为自动转换
    // var _value = typeof value === 'string' ? parseFloat(value) : value;
    var format = SystemConfiguration.read('format.float', '#,###.00')
    if (format != null && format != '') {
        text = NumberFormat.format(value, format);
    } else {
        text += value;
    }
    return text;
}

/**
 * 修饰 boolean 类型的呈现器。
 */
function BooleanCellRenderer() {

}

/**
 * 返回呈现器使用的控件
 */
BooleanCellRenderer.prototype.getRendererComponent = function (table, value, rowIndex, colIndex) {
    var text = "<input type='checkbox' value='' ";
    if (value != null && value) {
        text += "checked='true'";
    }

    text += "onclick='return false;' />";
    return text;
}

/**
 * 修饰“字典”类型的呈现器。
 */
function DictionaryCellRenderer(dicName) {
    if (dicName == null || dicName == '') {
        // throw new Error("字典名称不能为空。");
        throw new Error ("The name of dictionary can not be null.");
    }
    this.dic = DictionaryService.getDictionary(dicName);
}

/**
 * 返回“字典”使用的控件
 */
DictionaryCellRenderer.prototype.getRendererComponent = function (table, value, rowIndex, colIndex) {
    if (value == null) {
        return TableCellRenderer.NULL_VALUE_TEXT;
    }
    return DicUtils.getTitle(this.dic, value);
}

/**
 * 修饰图片类型的呈现器。
 * @param service 属性名称
 * @param imgConfig 图片的相关属性
 */
function ImageCellRenderer(service, methodName, params, imgAttrs) {
    if (service == null || methodName == null) {
        throw new Error("The parameters service and method name can not be null.");
    }
    this.service    = service;
    this.methodName = methodName;
    this.params     = params;
    this.imgAttrs    = imgAttrs;
}

/**
 * 返回呈现器使用的控件
 */
ImageCellRenderer.prototype.getRendererComponent = function (table, value, rowIndex, colIndex) {
    var imageId = 'img_' + IdGenerator.getId();
    var row = table.getRowAt(rowIndex);
    var params = this.params || [row];
    var url = ImageHelper.getImageUrl(this.service, this.methodName, params);
    var img = jQuery("<img id=" + imageId + " src='" + url + "' width='100%' height='100%' alt='图片错误'  />");
    // FF 在图像加载错误时不能触发load方法
    jQuery.each(this.imgAttrs, function (name, value) {
        img.attr(name, value);
    });
    return img;
}

/**
 * 表格呈现器工厂，用于根据表格属性确定表格呈现器的类型。
 */
var CellRendererFactory = {
    factories : {
        "java.util.Date"    : new DateCellRenderer(),
        "java.util.Calendar" : new DateCellRenderer(),
        "java.sql.Date"     : new DateCellRenderer(),
        "java.lang.Boolean" : new BooleanCellRenderer(),
        "boolean"           : new BooleanCellRenderer(),
        "java.lang.Integer" : new IntCellRenderer(),
        "int"               : new IntCellRenderer(),
        "java.lang.Long"    : new IntCellRenderer(),
        "long"              : new IntCellRenderer(),
        "java.lang.Enum"    : new EnumRenderer(),
        "java.lang.Double"  : new DoubleCellRenderer(),
        "double"            : new DoubleCellRenderer(),
        "java.lang.Float"   : new DoubleCellRenderer(),
        "float"             : new DoubleCellRenderer(),
        "java.lang.String"  : new DefaultCellRenderer()
    },

    getCellRenderer : function(tableColumnInfo) {
        var propertyClassName = tableColumnInfo.propertyClassName;
        if (tableColumnInfo.dictionaryInfo != null) {
            var di = tableColumnInfo.dictionaryInfo;
            return new DictionaryCellRenderer(di.name);
        } else if (tableColumnInfo.enumType) {
            // 如果属性类型为枚举类型
            return this.factories['java.lang.Enum'] || new DefaultCellRenderer();
        } else if (propertyClassName == null || this.factories[propertyClassName] == null) {
            return new DefaultCellRenderer();
        } else {
            return this.factories[propertyClassName];
        }
    }
}


var Pagination = function (startIndex, totalCount, rowsPerPage) {
    this.startIndex  = startIndex;
    this.totalCount  = totalCount;
    this.rowsPerPage = (rowsPerPage == null ? 20 : rowsPerPage);
}

Pagination.prototype = {
    nextPage : function () {
        // MessageBox.debug('this.totalCount=' + this.totalCount);
        if (this.hasNextPage()) {
            this.startIndex += this.rowsPerPage;
        }
    },
    
    hasNextPage : function() {
        // 无法确定总行数时，总是认为有下一页
        if (this.totalCount < 0) {
            return true;
        }
        return ((this.startIndex + this.rowsPerPage) < this.totalCount);
    },

    prevPage : function() {
        if (this.hasPrevPage()) {
            this.startIndex -= this.rowsPerPage;
            if (this.startIndex < 0) {
                this.startIndex = 0;
            }
        }
    },

    hasPrevPage : function() {
         return (this.totalCount == -1 || this.startIndex > 0);
    },

    firstPage : function() {
         this.startIndex = 0;
    },

    hasFirstPage : function() {
        return (this.startIndex > 0);
    },

    hasLastPage : function() {
        return (this.totalCount != -1 && (this.startIndex + this.rowsPerPage) < this.totalCount);
    },

    lastPage : function() {
		var mod = this.totalCount % this.rowsPerPage;
		return (mod == 0 ?  this.totalCount -  this.rowsPerPage:  this.totalCount - mod);
    },

    /**
     * 调整到指定页。
     */
    turnToPage : function(pageIndex) {
        if (pageIndex < 0 || pageIndex >= this.getPageCount()) {
            // throw new Error("页数[" + pageIndex + "]越界[" + 0 + ',' + this.getPageCount() + "]");
            throw new Error("The page no [" + pageIndex + "] is out of index[" + 0 + ',' + this.getPageCount() + "]");
        }
        this.startIndex = pageIndex * this.rowsPerPage;
    },
    
    /**
     * 返回总页数，如果记录数为 0，这返回0；如果记录数为“未知”，则返回“-1”,
     * 其他情况根据当前的总数算出中页数。
     */
    getPageCount : function() {
        if (this.totalCount <= 0) {
            return this.totalCount;
        }
        var totalPages =  Math.floor(this.totalCount /  this.rowsPerPage);
		var mod =  this.totalCount %  this.rowsPerPage;
		return (mod == 0 ? totalPages : totalPages + 1);
    },

    /**
     * 得到当前的页号
     */
    getCurrentPageIndex : function() {
        if (this.startIndex <= 0) {
            return 0;
        }
        var pageIndex =  Math.floor(this.totalCount /  this.startIndex);
        return (pageIndex);
    },

    /**
     * 将分页参数设置为初始状态。
     */
    reset : function () {
        this.totalCount = -1;
        this.startIndex = 0;
    }
    
}

var FilterType = {
    EQUALS            : "EQUALS",
    STARTS_WITH       : "STARTS_WITH",
    ENDS_WITH         : "ENDS_WITH",
    BETWEEN          : "BETWEEN",
    IN               : "IN",
    CONTAINS          : "CONTAINS",
    LESS_THAN        : "LESS_THAN",
    GREAT_THAN       : "GREAT_THAN",
    LESS_EQUAL_THAN  : "LESS_EQUAL_THAN",
    GREAT_EQUAL_THAN : "GREAT_EQUAL_THAN",
    IS_NULL          : "IS_NULL",
    IS_EMPTY         : "IS_EMPTY",
    CUSTOMIZED       : "CUSTOMIZED"
}

function SimpleFilterItem (name, type) {
    this.name = name;
    this.type = type;
    // this.not  = false;
    this.values = new Array();
}

function FilterGroup () {
    this.filterItems = new Array();
}

function FspParameter () {
    this.pagination = new Pagination(0, -1);
    // sorts
    //
    this.filterGroup = new FilterGroup();

    this.setStartIndex = function(startIndex) {
        this.pagination.startIndex = startIndex;
    }

    /**
     *  重新设置查询组，即清空现在已有的所有查询选项
     */
    this.resetFilterGroup = function () {
        this.filterGroup = new FilterGroup();
    }

    /**
     *  设置过滤条件
 */
    this.setFilterItem = function (name, filterType, value) {
        // 剔除重复的过滤
        jQuery.each(this.filterGroup.filterItems, function(index, filterItem) {
            if (name == filterItem.name && filterType == filterItem.type) {
                // TODO: 在调试的模式下运行要抛出异常
                // throw new Error("过滤条件名称[" + name + "]类型[" + filterType + "]已经存在。");
                throw new Error("The name of filter item[" + name + "] and type [" + filterType + "] existed.");
                // return;
            }
        });
        var sfi = new SimpleFilterItem(name, filterType);
        if (value instanceof Array) {
            for (var i = 0; i < value.length; i++) {
                sfi.values.push(value[i]);
            }
        } else {
            sfi.values.push(value);
        }
        this.filterGroup.filterItems.push(sfi);
    }

   /**
    * 清空所有的过滤条件
    */
    this.clearFilterItem = function () {
        this.filterGroup.filterItems = [];
    }
}

/**
 * 定义表行记录的呈现器。
 */
function RowRenderer () {
    /**
     * 返回当前行的字符串
     */
    this.getRow = function (row, rowIndex){};
}

/**
 * 使用模板方式创建行记录
 * @param id 模板所在的位置
 * @param process 值处理函数, 其接口形式为： process(row, rowIndex, varName, value),
 * 返回一个待显示的字符串。
 */
function TemplateRowRenderer (id, process) {
    this.id = id;
    this.process = process;

    // 变量的匹配模式
    this.varPattern = /\$\{([a-zA-Z_]\w*)\}/g;
}

jQuery.extend(TemplateRowRenderer.prototype, {
    /**
     * 返回当前行的字符串
     */
    getRow : function (row, rowIndex) {
        var text = jQuery("#" + this.id).html();
        // 实施参数替换
        var matches = text.match(this.varPattern);
        if (matches == null) {
            return text;
        }
        var resultStr = text;
        for (var i = 0; i < matches.length; i++) {
            var varName = matches[i].substring(2, matches[i].length - 1);
            var value = row[varName];
            if (this.process != null) {
                value = this.process(row, rowIndex, varName, value);
            }
            resultStr = resultStr.replace(matches[i], value);
        }
        return resultStr;
    }
});

/**
 * 用于封装表格相关的操作类。
 */
var TableUtils = function (tableDom, tableConfig, serviceClass, params) {
    if (tableDom == null || tableConfig == null || serviceClass == null) {
        throw new Error("Parameters tableDom(1), tableConfig(2) and serviceClass(3) can not be null.");
    }
    this.tableDom     = tableDom;    // _createJQuery(tableDom);  // 表模型
    this.tableConfig  = tableConfig;                // 表配置
    this.serviceClass = serviceClass;               // 服务类
    this.methodName   = 'list';                     // 查询数据的方法名称，默认为：list
    this.table        = new GTable(this.tableDom);  // 表实例
    this.service      = null;                       // 服务实例

    // 读取参数
    if (params != null) {
        this.methodName     = params['methodName'] || this.methodName;  // 查询数据的方法
        this.methodParams   = params['methodParams'];          // 查询方法的参数（不需要包括过滤参数）
        this.query          = params['query'];                 // 自动绑定查询，参数值是一个Map，包括：区域ID，查询回调的动作等等
        this.paginationConfig = params['pagination'];          // 分页相关参数
        this.actionsConfig    = params['actionsConfig'];       // 处理和表相关的操作
     }
}

/**
     * 创建对话框。
     */
    TableUtils._createDialog = function (dialogId, params) {
        var dialog = window[dialogId];
        if (dialog == null) {
            dialog = jQuery("<div id='" + dialogId + "'></div>");
            // 初次初始化页面时使用 URL 传递参数
            // url = ParameterHelper.append(url, params);
            dialog.load(params.editForm, {}, function() {});
            var table = params.table;
            var cancelClicked = params.cancelClicked;
            var okClicked = params.okClicked;
            var _editingRowIndex = params['editingRowIndex'];
            var _editingRow      = params['editingRow'];
            dialog.dialog({
        	    autoOpen: false,
	            width: 600,
                title: '数据编辑',
	            modal: true,
	            buttons: {
			        "取消": function() {
                        if (cancelClicked == null || cancelClicked(_editingRow, table)) {
                            // 清除此参数
                            // delete window.__editingParams;
				            dialog.dialog("close");
                        }
			        },

			        "确定": function() {
                        var ret = okClicked(params.service, _editingRow, _editingRowIndex, params);  // 更新的返回结果
                        if (ret) {
                            // 清除此参数
                            // delete window.__editingParams;
				            dialog.dialog("close");
                        }
			        }
	            }
            });

            dialog.bind('dialogopen', function(event, ui) {
                // 再次调用页面时，使用方法接口传递参数
                if (window.formReentry) {
                    window.formReentry(window.__editingParams);
                }
            });
            window[dialogId] = dialog;
        }
        window.__editingParams = params;
        return dialog;
    };

TableUtils.prototype = {

    // 自动装载表的操作
    autoLoad : function () {
        
        // 从服务器读取数据
        this.service = ServiceFactory.getService(this.serviceClass);
        
        var data = this._loadData();

        // 装载表
        this.table.decorate(this.tableConfig, data);

        // 绑定查询操作
        this.bindQuery();

        this.buildPagination();

        this.handleActions();
        
        return this.table;
    },

    /**
     * 通过服务器装载数据
     */
    _loadData : function () {
	    //MessageBox.debug('methodParams=', this.methodParams);
        var data = this.service[this.methodName].apply(this.service, this.methodParams || []);

        // 将服务器参数赋值为本地参数
        if (this.table.fspParameter == null) {
            this.table.fspParameter = new FspParameter();
        }
        Utils.valueCopy(this.service.fspParameter, this.table.fspParameter);
        this._setPaginationButtonState(this.table.fspParameter.pagination);
        return data;
    },

    /**
     * 刷新当前数据
     */
    refresh : function() {
        Utils.valueCopy(this.service.fspParameter, this.table.fspParameter);
	var data = this._loadData();
	this.table.reload(data);
    },

    /**
     * 将表和查询页进行绑定。
     */
    bindQuery : function () {
        if (!this.query) {
            return;
        }
        
        var queryDom = _createJQuery(this.query['dom']);
        if (queryDom == null) {
            // throw new Error ("查询区域的Dom对象不能为空。");
            throw new Error ("The dom of query can not be null.");
        }
        
        // 查询处理事件
        var queryAction = function(event) {
            /* alert('queryAction............'); */
            var table = event.data.table;
            var comps = ControlUtils.getNamedComp(queryDom);
            // 清除过滤条件，避免重复
            table.fspParameter.clearFilterItem();
            for (var name in comps) {
                var comp = jQuery(comps[name]);
                var filterType = comp.attr('filterType');
                 // alert("input[" + this.name + "]; value=" + this.value + ";filterType=" + filterType);
                 var value = comp.val();
	             if (name != '' && value != null && value != '' && filterType != null) {
	                table.fspParameter.setFilterItem(name, filterType, value);
	             }
            }
            var service = event.data.service;
            service.fspParameter = null;
            table.fspParameter.pagination.reset();  // 重置分页参数
            service.setFspParameter(table.fspParameter);
            
            // 加载数据
            var data = event.data.tableUtils._loadData(table, service);

            table.reload(data);

            // 查询回调函数
            if (event.data['onQuery'] != null) {
                event.data['onQuery'](table, data, table.fspParameter);
            }
        }

        var queryBtn = _createJQuery(this.query['button']);  // 查询按钮对象
        if (queryBtn != null) {
            queryBtn.bind("click", {table : this.table, 
                service : this.service, onQuery : this.query['onQuery'],
                tableUtils : this}, queryAction);
        }
    },

    /**
     * 创建分页相关的信息
     */
    buildPagination : function () {
        if (this.paginationConfig == null) {
            this.paginationConfig = {};
        }
        if (true === this.paginationConfig['disable']) {
            // 定义了disalbe属性时，分页才不起作用
            return;
        }

        var auto = (true === this.paginationConfig['auto']);              // 是否根据模板自动创建分页（在指定的区域动态生成按钮）
        
        // 翻页处理函数
        var turnPageAction = function (event) {
            var table = event.data.table;
            var action = event.data.action;
            var params = jQuery(this).val();    // 跳转到指定页时会使用此参数
            table.fspParameter.pagination[action](params);

            // 设定翻页的值
            var turnPageComboBox = event.data.turnPageComboBox;
            if (turnPageComboBox != null) {
                jQuery(turnPageComboBox).val(table.fspParameter.pagination.getCurrentPageIndex());
            }

            var service = event.data.service;
            // TODO: 重新创建过滤条件的值（即在用户改变了值之后，未进行查询的情况下）
            service.setFspParameter(table.fspParameter);
            var data = event.data.tableUtils._loadData(table, service);
	        table.reload(data);
        } // end of turnPageAction

        if (auto) {
            // TODO: 自动生成翻页按钮?
            
        } else {
            // 根据已有的翻页按钮进行处理
            var turnPageComboBox = this.paginationConfig['turnPage'];
            if (turnPageComboBox != null) {
                this._buildTurnPage(turnPageComboBox, this.table.fspParameter.pagination, turnPageAction);
            }

            var nextPage       = _createJQuery(this.paginationConfig['nextPage']  || '#nextPage');
            var prevPage       = _createJQuery(this.paginationConfig['prevPage']  || '#prevPage');
            var firstPage      = _createJQuery(this.paginationConfig['firstPage'] || '#firstPage');
            var lastPage       = _createJQuery(this.paginationConfig['lastPage']  || '#lastPage');
            nextPage.bind("click", this._getPaginationParams('nextPage', turnPageComboBox), turnPageAction);
            prevPage.bind("click", this._getPaginationParams('prevPage', turnPageComboBox), turnPageAction);
            firstPage.bind("click",this._getPaginationParams('firstPage', turnPageComboBox),turnPageAction);
            lastPage.bind("click", this._getPaginationParams('lastPage', turnPageComboBox), turnPageAction);            
        }
    },
    
    /**
     * TODO: 创建一个“跳转到指定页”的下拉列表框。用户可自定义翻页面板。
     */
    _buildTurnPage : function (comboBox, pagination, turnPageAction) {
        // 行数是从1开始计数的
        var _comboBox = jQuery(comboBox);
        _comboBox.bind('change', this._getPaginationParams('turnToPage'), turnPageAction);
        for (var i = 0; i < pagination.getPageCount(); i++) {
            var option = jQuery("<option></option>");
            option.val(i).text(i + 1);
            option.appendTo(_comboBox);
        }
    },

    /**
     * 返回翻页相关的参数(所有按钮公用的参数)
     */
    _getPaginationParams : function (_action, turnPageComboBox) {
        var pgParams = {table : this.table, service : this.service, methodName : this.methodName,
                methodParams : this.methodParams, tableUtils: this, turnPageComboBox : turnPageComboBox,
                action : _action};
        return pgParams;
    },

    /**
     * 处理和表的一些相关动作
     */
    handleActions : function() {
        if (!this.actionsConfig) {
            return;
        }
        var addBtn = _createJQuery(this.actionsConfig['add']);
        if (addBtn) {
            var editForm     = this.actionsConfig['editForm'];      // 用户定义的更新页面
            if (editForm == null) {
                // throw new Error("未定义编辑表单参数[editForm]。");
                throw new Error("The parameter [editForm] is undefined.");
            }
            
            // 可以自定义一个初始化
            var newInstance = this.actionsConfig['newInstance'];
            var editingRow  = newInstance ? newInstance() : {};  // 初始化一个（TODO: 初始化__className）
            var addAction   = this.actionsConfig['addAction'];   // 用户定义的增加操作
            // 用户可以单独定义的增加页面，如果未定义，则使用编辑页面
            var editForm    = this.actionsConfig['addForm'] || this.actionsConfig['editForm'];
            addBtn.bind("click",
                {table : this.table, service : this.service, addAction : addAction,
                 editForm : editForm, editingRow : editingRow},
                this.addRow);
        }

        var updateBtn = _createJQuery(this.actionsConfig['update']);
        if (updateBtn) {
            // 使用删除的增加函数
            var updateAction = this.actionsConfig['updateAction'];  // 用户定义的更新操作
            var editForm     = this.actionsConfig['editForm'];      // 用户定义的更新页面
            if (editForm == null) {
                // throw new Error("未定义编辑表单参数[editForm]。");
                throw new Error("The parameter [editForm] is undefined.");
            }
            updateBtn.bind("click", 
                {table : this.table, service : this.service, updateAction : updateAction,
                 editForm : editForm},
                this.updateRow);

            // 更新按钮时的回调函数
            // var updateBtnCallback = this.actionsConfig['updateCallback'];
        }

        var removeBtn = _createJQuery(this.actionsConfig['remove']);
        if (removeBtn) {            
            // 使用删除的增加函数
            var removeAction = this.actionsConfig['removeAction'];  // 用户定义的删除操作
            removeBtn.bind("click", {table : this.table, service : this.service, removeAction : removeAction},
                this.removeRow);
        }
    },
    
    /**
     * 增加并编辑一行记录。
     */
    addRow : function (event) {
        var table      = event.data.table;
        var editingRow = event.data.editingRow;
        var okClicked = function (service, editingRow) {
             var ret = false;  // 更新的返回结果
             if (window.addAction != null) {
                 ret = window.addAction(editingRow, table, service);
             } else {
                 var daa = new DefaultAddAction(service);
                 ret = daa.addAction(editingRow, table, service);
             }
             if (ret) {
                  table.appendRow(editingRow);
             }
             return ret;
        };
        var params = {okClicked : okClicked, action : 'add', bindingClass : table.getBindingClass(),
            editingRow : editingRow};
        jQuery.extend(params, event.data);
        var dialog = TableUtils._createDialog('__auto__dialog__', params);

        // 弹出修改对话框
        dialog.dialog('open');
    },
    
    /**
     * 删除处理函数
     */
    removeRow : function (event) {
        var table = event.data.table;
        var rowIndex = table.getSelectedRowNum();
        if (rowIndex >= 0) {
	        var selectedRow = table.getRowAt(rowIndex);
            var service = event.data.service;
	        // 在服务器端删除此记录
            var removeAction = event.data['removeAction'];
            var success = false;
            if (!confirm('确认删除此记录？')) {
                return;
            }
            if (removeAction != null) {
                // 调用用户自定义的删除方法
                success = removeAction(table, selectedRow, service);
            } else {
                // 使用标准的删除方法
                success = service['delete'](selectedRow);
            }
    	    if (success) {
                table.deleteRow(rowIndex);
	            MessageBox.inform("删除成功！");
	        } else {
	            MessageBox.alert("删除失败！");
	        }
        } else {
	        MessageBox.alert("请选择待删除的记录。");
    	}
    },

    updateRow : function(event) {
        var table = event.data.table;
        var editingRowIndex = table.getSelectedRowNum();
	    if (editingRowIndex < 0) {
	        MessageBox.alert('请选择待修改的行。');
		    return;
        }

        var okClicked = function (service, editingRow, editingRowIndex) {
             var ret = false;  // 更新的返回结果
             if (window.updateAction != null) {
                 ret = window.updateAction(editingRow, table, service);
             } else {
                 var dua = new DefaultUpdateAction(service);
                 ret = dua.updateAction(editingRow, table, service);
             }
             if (ret) {
                  table.updateRow(editingRowIndex, editingRow);
             }
             return ret;
        };
        var params = {okClicked : okClicked, action : 'update', bindingClass : table.getBindingClass(),
            editingRowIndex : editingRowIndex, editingRow : table.getRowAt(editingRowIndex)};
        jQuery.extend(params, event.data);
        var dialog = TableUtils._createDialog('__auto__dialog__', params);

        // 弹出修改对话框
        dialog.dialog('open');
    },
    

    /**
     * 根据翻页参数设置翻页按钮的状态
     */
    _setPaginationButtonState : function (pagination, paginationConfig) {
        if (pagination == null) {
            return;
        }
        if (paginationConfig == null) {
            paginationConfig = {};
        }
        var nextPage  = _createJQuery(paginationConfig['nextPage']  || '#nextPage');
        var prevPage  = _createJQuery(paginationConfig['prevPage']  || '#prevPage');
        var firstPage = _createJQuery(paginationConfig['firstPage'] || '#firstPage');
        var lastPage  = _createJQuery(paginationConfig['lastPage']  || '#lastPage');
        
        //TODO： 如果按钮是链接的方式，需要考虑其他方法,用其他方式修饰（指定CSS）
        nextPage.attr('disabled', pagination.hasNextPage()   ? '' : 'true');
        prevPage.attr('disabled', pagination.hasPrevPage()   ? '' : 'true');
        firstPage.attr('disabled', pagination.hasFirstPage() ? '' : 'true');
        lastPage.attr('disabled', pagination.hasLastPage()   ? '' : 'true');
    }
}

/**
 * 缺省的更新方法操作，主要的功能是调用和装载服务对象的“update”方法，
 * 此方法只能使用一个对象作为参数-即当前的编辑行。
 */
var DefaultAddAction = function (_service) {
    if (_service == null) {
        // throw new Error("服务类不能为空。");
        throw new Error("The service class can not be null.");
    }
    this.service = _service;
    this.methodName = 'add';
}

jQuery.extend(DefaultAddAction.prototype, {
    addAction : function (editingRow, table) {
        // return this.service[this.methodName](editingRow);
        this.service[this.methodName](editingRow);
        return true;
    }
});

/**
 * 缺省的更新方法操作，主要的功能是调用和装载服务对象的“update”方法，
 * 此方法只能使用一个对象作为参数-即当前的编辑行。
 */
var DefaultUpdateAction = function (_service) {
    if (_service == null) {
        // throw new Error("服务类不能为空。");
        throw new Error("The service class can not be null.");
    }
    this.service = _service;
    this.methodName = 'update';
}

jQuery.extend(DefaultUpdateAction.prototype, {
    updateAction : function (editingRow, table) {
        // return this.service[this.methodName](editingRow);
        this.service[this.methodName](editingRow);
        return true;
    }
});


/**
 * 和表单呈现器一些相关的辅助 API。
 */
var FormEditor = {
    getParams : function () {
        return window.__editingParams;
    },

    init : function (form, config) {
        var params = this.getParams();
        BindUtils.bind(form, params['editingRow']);
        // 绑定校验关系
        var vu = new Validator();
        vu.bindContainer(form, params['bindingClass']);
        return vu;
    }
}

/**
 * 用于简化“主-子表”结构的数据处理引入的一个简化类。
 * @param masterTable 主表（GTable对象），不能为空。
 * @param load 数据装载接口，形式为：load(selectedRow), 返回值为：数据数组
 */
function MasterDetailTable(masterTable, load) {
    if (masterTable == null) {
        throw new Error("The parameter[masterTable] can not be null.");
    }
    this.masterTable  = masterTable;
    this.detailTables = {};
    this.masterTable.addRowSelectionListener(new MasterDetailsRowSelection (masterTable, this.detailTables, load));
}

jQuery.extend(MasterDetailTable.prototype, {
    /**
     * 给当前主表增加一个子表。
     * @param detailTable 子表（GTable对象），不能为空。
     * @param propertyName 和子表对应的主表的绑定类的属性名称（代码将判断这个属性是否为空，以确定是否加载）。
     */
    addChild : function(propertyName, detailTable) {
        this.detailTables[propertyName] = detailTable;
    }
});

// 根据当前的选择情况，切换相关的信息
function MasterDetailsRowSelection(masterTable, detailTables, load) {
    this.masterTable  = masterTable;   // 主表
    this.detailTables = detailTables;  // 所有子表
    this.load         = load;
    
    this.onRowSelection = function(event) {
        if (this.detailTables == null) {
            return;
        }
	    var selectedRowIndex = event.rowIndex;
		if (selectedRowIndex < 0 ){
			// 清空原有内容
            jQuery.each(this.detailTables, function(propertyName, detailTable) {
                detailTable.clear();
            });
		    return;
		}
		var selectedUser = masterTable.getRowAt(selectedRowIndex);
        var load = this.load;
		// 子表信息
        jQuery.each(this.detailTables, function(propertyName, detailTable) {
            if (selectedUser[propertyName] == null) {
		        // 用户定义加载方式
                var data = load(propertyName, selectedUser);
			    selectedUser[propertyName] = data || [];
		    }
            detailTable.reload(selectedUser[propertyName]);
        });		
	}
}

