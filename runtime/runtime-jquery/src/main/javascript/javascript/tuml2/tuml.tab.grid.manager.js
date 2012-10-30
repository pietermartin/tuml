(function ($) {
    // register namespace
    $.extend(true, window, {
        Tuml: {
            TumlTabGridManager: TumlTabGridManager
        }
    });

    function TumlTabGridManager(tumlUri) {

        var self = this;

        function init() {
        }

        function refresh(result) {
            var metaForData = result.meta[2];
            var tabDiv = $('#' + metaForData.name);
            var myGridDiv = $('<div id="myGrid' + metaForData.name + '" style="width:100%;height:90%;"></div>').appendTo(tabDiv);
            var pagerDiv = $('<div id="pager' + metaForData.name + '" style="width:100%;height:20px;"></div>').appendTo(tabDiv);
            var t1 = $('#myGrid' + metaForData.name);
            var qualifiedName = result.meta[0].qualifiedName;
            var data = result.data;
            $('#contextMenu' + metaForData.name).remove();
            createGrid(data, metaForData, tumlUri);
        }

        //Public api
        $.extend(this, {
            "TumlTabGridManagerVersion": "1.0.0",
            "onPutSuccess": new Tuml.Event(),
            "onPutFailure": new Tuml.Event(),
            "onPostSuccess": new Tuml.Event(),
            "onPostFailure": new Tuml.Event(),
            "onDeleteSuccess": new Tuml.Event(),
            "onDeleteFailure": new Tuml.Event(),
            "onCancel": new Tuml.Event(),
            "onContextMenuClickLink": new Tuml.Event(),
            "onContextMenuClickDelete": new Tuml.Event(),
            "refresh": refresh
        });

        function createGrid(data, metaForData, tumlUri) {
            var grid;
            var columns = [];
            var columnFilters = {};
            var lookupColumns;

            $.each(metaForData.properties, function(index, property) {
                if (!property.inverseComposite && (property.oneToOne || property.manyToOne)) {
                    var contextVertexId = tumlUri.match(/\d+/);
                    //Place the id column first
                    if (property.name == "id") {
                        columns.splice(0,0,{
                            id: property.name,
                            name: property.name,
                            field: property.name,
                            sortable: true,
                            formatter: selectFormatter(property)
                        });           
                    } else {
                        columns.push({
                            id: property.name,
                            name: property.name,
                            field: property.name,
                            sortable: true,
                            editor: selectEditor(property),
                            formatter: selectFormatter(property),
                            validator: selectFieldValidator(property),
                            options: {required: property.lower > 0, tumlLookupUri: property.tumlLookupUri,rowEnumerationLookupMap: new RowEnumerationLookupMap(property.qualifiedName, "/restAndJson/tumlEnumLookup"),  rowLookupMap: new RowLookupMap(contextVertexId, property.tumlCompositeParentLookupUri, property.tumlCompositeParentLookupUriOnCompositeParent), compositeParentLookupMap: new CompositeParentLookupMap(contextVertexId, property.tumlLookupUri, property.tumlLookupUriOnCompositeParent)},
                            width: 120
                        });
                    }
                }
            });
            columns.push({id: "uri", name: "uri", field: "uri", sortable: false, formatter: TumlSlick.Formatters.Link});
            columns.push(
                {id: "delete", name: "delete", field: "delete", sortable: false, 
                    formatter: TumlSlick.Formatters.TumlDelete }
            );

            var options = {
                showHeaderRow: true,
                headerRowHeight: 30,
                editable: true,
                enableAddRow: true,
                enableCellNavigation: true,
                asyncEditorLoading: false,
                enableAsyncPostRender: true,
                forceFitColumns: false,
                topPanelHeight: 25
            };

            var sortcol = "name";
            var sortdir = 1;
            var percentCompleteThreshold = 0;
            var searchString = "";

            var dataView = new Tuml.Slick.Data.DataView();
            grid = new Slick.Grid("#myGrid" + metaForData.name , dataView, columns, options);
            grid.setSelectionModel(new Slick.RowSelectionModel());
            var pager = new Slick.Controls.Pager(dataView, grid, $("#pager" + metaForData.name));
            var columnpicker = new Slick.Controls.ColumnPicker(columns, grid, options);
            $("<div id='grid-button" + metaForData.name + "' class='grid-button'/>").appendTo('#pager' + metaForData.name + ' .slick-pager-settings');

            var $saveButton = $('<button />').text('Save').click(function() {
                if (grid.getEditorLock().commitCurrentEdit()) {
                    //put updated items
                    if (dataView.getUpdatedItems().length !== 0) {
                        $.ajax({
                            url: tumlUri,
                            type: "PUT",
                            dataType: "json",
                            contentType: "json",
                            data: JSON.stringify(dataView.getUpdatedItems()),
                            success: function(data, textStatus, jqXHR) {
                                self.onPutSuccess.notify({tumlUri: tumlUri, tabId: metaForData.name, data: data}, null, self);
                            },
                            error: function(jqXHR, textStatus, errorThrown) {
                                self.onPutFailure.notify({tumlUri: tumlUri, tabId: metaForData.name}, null, self);
                            }
                        });
                    }
                    //post new items
                    if (dataView.getNewItems().length !== 0) {
                        $.ajax({
                            url: tumlUri,
                            type: "POST",
                            dataType: "json",
                            contentType: "json",
                            data: JSON.stringify(dataView.getNewItems()),
                            success: function(data, textStatus, jqXHR) {
                                self.onPostSuccess.notify({tumlUri: tumlUri, tabId: metaForData.name, data: data}, null, self);
                            },
                            error: function(jqXHR, textStatus, errorThrown) {
                                self.onPostFailure.notify({tumlUri: tumlUri, tabId: metaForData.name}, null, self);
                                //alert("error\n" + "status = " + jqXHR.status + "\n" + jqXHR.responseText);
                            }
                        });
                    }
                    //delete new items
                    if (dataView.getDeletedItems().length !== 0) {
                        $.ajax({
                            url: tumlUri,
                            type: "DELETE",
                            dataType: "json",
                            contentType: "json",
                            data: JSON.stringify(dataView.getDeletedItems()),
                            success: function(data, textStatus, jqXHR) {
                                self.onDeleteSuccess.notify({tumlUri: tumlUri, tabId: metaForData.name, data: data}, null, self);
                            },
                            error: function(jqXHR, textStatus, errorThrown) {
                                //alert("error\n" + "status = " + jqXHR.status + "\n" + jqXHR.responseText);
                                self.onDeleteFailure.notify({tumlUri: tumlUri, tabId: metaForData.name}, null, self);
                            }
                        });
                    }
                } else {
                    alert("Commit failed on current active cell!");
                }
            }).appendTo('#grid-button' + metaForData.name);

            var $cancelButton = $('<button />').text('Cancel').click(function() {
                //refreshPageTo(tumlUri);
                if (grid.getEditorLock().commitCurrentEdit()) {
                    $.ajax({
                        url: tumlUri,
                        type: "GET",
                        dataType: "json",
                        contentType: "json",
                        data: JSON.stringify(dataView.getDeletedItems()),
                        success: function(data, textStatus, jqXHR) {
                            self.onCancel.notify({tumlUri: tumlUri, tabId: metaForData.name, data: data}, null, self);
                        },
                        error: function(jqXHR, textStatus, errorThrown) {
                            //alert("error\n" + "status = " + jqXHR.status + "\n" + jqXHR.responseText);
                        }
                    });
                }
            }).appendTo('#grid-button' + metaForData.name);

            //Create context menu
            var contextMenuUl = $('<ul />', {id: 'contextMenu' + metaForData.name, style: 'display:none;position:absolute', class: 'contextMenu'}).appendTo('body');
            $('<b />').text('Nav').appendTo(contextMenuUl);
            $('<li data="' + metaForData.uri + '" />').text("self").appendTo(contextMenuUl);
            $.each(metaForData.properties, function(index, property) {
                if (property.inverseComposite || !((property.dataTypeEnum !== undefined &&  property.dataTypeEnum !== null) || property.onePrimitive || property.manyPrimitive || property.name == 'id' || property.name == 'uri')) {
                    $('<li data="' + property.tumlUri + '" />').text(property.name).appendTo(contextMenuUl);
                }
            });
            $('<li data="delete" />').text("delete").appendTo(contextMenuUl);

            grid.onContextMenu.subscribe(function (e) {
                e.preventDefault();
                var cell = grid.getCellFromEvent(e);
                $("#contextMenu" + metaForData.name)
                .data("row", cell.row)
                .css("top", e.pageY)
                .css("left", e.pageX)
                .show();

                $("body").one("click", function () {
                    $("#contextMenu" + metaForData.name).hide();
                });
                $('#contextMenu' + metaForData.name).mouseleave(contextMenu_timer);
            });

            function contextMenu_close() {  
                $("#contextMenu" + metaForData.name).hide();
            }

            function contextMenu_timer() {  
                closetimer = window.setTimeout(contextMenu_close, 200);
            }

            $("#contextMenu" + metaForData.name).click(function (e) {
                if (!$(e.target).is("li")) {
                    return;
                }
                var row = $(this).data("row");
                var tumlUri = $(e.target).attr("data");
                if (tumlUri !== 'delete') {
                    var url = tumlUri.replace(new RegExp("\{(\s*?.*?)*?\}", 'gi'), data[row].id);
                    //change_my_url("unused", url);
                    //refreshPageTo(url);
                    self.onContextMenuClickLink.notify({name: 'unused', tumlUri: url}, null, self);
                } else {
                    var item = dataView.getItem(row);
                    dataView.deleteItem(item.id);
                    //self.onContextMenuClickDelete.notify({tumlUri: tumlUri, tabId: metaForData.name}, null, self);
                }
            }); 

            grid.onActiveCellChanged.subscribe(function(e, args) {
                if (grid.getColumns()[args.cell].name == 'delete') {
                    var item = dataView.getItem(args.row);
                    dataView.deleteItem(item.id);
                }
            });

            grid.onCellChange.subscribe(function(e, args) {
                dataView.updateItem(args.item.id, args.item, grid.getColumns()[args.cell]);
            });

            grid.onAddNewRow.subscribe(function(e, args) {
                var $newItem = {};
                for (i = 0; i < grid.getColumns().length; i++) {
                    var column = grid.getColumns()[i];
                    $newItem[column.name] = null;
                }
                //Generate a fake id, its required for the grid to work nicely
                $newItem.id = 'fake::' + data.length + dataView.getNewItems().length + 1;
                dataView.addItem($.extend($newItem, args.item));
            });

            grid.onKeyDown.subscribe(function(e) {
                // select all rows on ctrl-a
                if (e.which != 65 || !e.ctrlKey) {
                    return false;
                }

                var rows = [];
                for (var i = 0; i < dataView.getLength(); i++) {
                    rows.push(i);
                }

                grid.setSelectedRows(rows);
                e.preventDefault();
            });

            grid.onSort.subscribe(function(e, args) {
                sortdir = args.sortAsc ? 1 : -1;
                sortcol = args.sortCol.field;

                if ($.browser.msie && $.browser.version <= 8) {
                    // using temporary Object.prototype.toString override
                    // more limited and does lexicographic sort only by default, but can be much faster
                    var percentCompleteValueFn = function() {
                        var val = this["percentComplete"];
                        if (val < 10) {
                            return "00" + val;
                        } else if (val < 100) {
                            return "0" + val;
                        } else {
                            return val;
                        }
                    };

                    // use numeric sort of % and lexicographic for everything else
                    dataView.fastSort((sortcol == "percentComplete") ? percentCompleteValueFn : sortcol, args.sortAsc);
                } else {
                    // using native sort with comparer
                    // preferred method but can be very slow in IE with huge datasets
                    dataView.sort(function comparer(a, b) {
                        var x = a[sortcol],
                        y = b[sortcol];
                        return (x == y ? 0 : (x > y ? 1 : -1));
                    }, args.sortAsc);
                }
            });

            // wire up model events to drive the grid
            dataView.onRowCountChanged.subscribe(function(e, args) {
                grid.updateRowCount();
                grid.render();
            });

            dataView.onRowsChanged.subscribe(function(e, args) {
                grid.invalidateRows(args.rows);
                grid.render();
            });

            dataView.onPagingInfoChanged.subscribe(function(e, pagingInfo) {
                var isLastPage = pagingInfo.pageNum == pagingInfo.totalPages - 1;
                var enableAddRow = isLastPage || pagingInfo.pageSize == 0;
                var options = grid.getOptions();

                if (options.enableAddRow != enableAddRow) {
                    grid.setOptions({
                        enableAddRow: enableAddRow
                    });
                }
            });

            $(grid.getHeaderRow()).delegate(":input", "change keyup", function(e) {
                columnFilters[$(this).data("columnId")] = $.trim($(this).val());
                dataView.refresh();
            });

            grid.onColumnsReordered.subscribe(function(e, args) {
                updateHeaderRow(metaForData);
            });

            grid.onColumnsResized.subscribe(function(e, args) {
                updateHeaderRow(metaForData);
            });

            grid.onViewportChanged.subscribe(function (e, args) {
                var vp = grid.getViewport();
            });
            grid.onViewportChanged.notify();

            function updateHeaderRow(metaForData) {
                for (var i = 0; i < columns.length; i++) {
                    if (columns[i].id !== "selector" && columns[i].id !== 'uri' && columns[i].id !== 'delete') {
                        var header = grid.getHeaderRowColumn(columns[i].id);
                        $(header).empty();
                        $(constructHeaderRowBox(columns[i].id, metaForData)).data("columnId", columns[i].id).val(columnFilters[columns[i].id]).appendTo(header);
                    }
                }
            }

            function constructHeaderRowBox(columnId, metaForData) {
                for (var i = 0; i < metaForData.properties.length; i++) {
                    var property = metaForData.properties[i];
                    if (property.name == columnId && property.fieldType == 'String') {
                        return "<input type='text'>";
                    } else if (property.name == columnId && (property.fieldType == 'Integer' || property.fieldType == 'Long')) {
                        return "<input type='text'>";
                    } else if (property.name == columnId && property.fieldType == 'Boolean') {
                        //return "<INPUT type=checkbox value='true' class='editor-checkbox' hideFocus>";
                        return "<select><option value='None'>None</option><option value='true'>True</option><option value='false'>False</option></select>";
                    } else {
                        //return "<input type='text'>";
                    }
                }
                return "<input type='text'>";
            }

            function filter(item, metaForData) {
                for (var columnId in columnFilters) {
                    if (columnId !== undefined && columnFilters[columnId] !== "") {
                        var c = grid.getColumns()[grid.getColumnIndex(columnId)];
                        for (var i = 0; i < metaForData.metaForData.properties.length; i++) {
                            var property = metaForData.metaForData.properties[i];
                            if (property.name == columnId) {
                                if (property.dataTypeEnum !== undefined) {
                                    if (property.dataTypeEnum == 'Date') {
                                        if (item[c.field] == null || item[c.field].indexOf(columnFilters[columnId]) == -1) {
                                            return false;
                                        }
                                    } else if (property.dataTypeEnum == 'Time') {
                                        if (item[c.field] == undefined || item[c.field].indexOf(columnFilters[columnId]) == -1) {
                                            return false;
                                        }
                                    } else if (property.dataTypeEnum == 'DateTime') {
                                        if (item[c.field] == undefined || item[c.field].indexOf(columnFilters[columnId]) == -1) {
                                            return false;
                                        }
                                    } else if (property.dataTypeEnum == 'InternationalPhoneNumber') {
                                        if (item[c.field] == undefined || item[c.field].indexOf(columnFilters[columnId]) == -1) {
                                            return false;
                                        }
                                    } else if (property.dataTypeEnum == 'LocalPhoneNumber') {
                                        if (item[c.field] == undefined || item[c.field].indexOf(columnFilters[columnId]) == -1) {
                                            return false;
                                        }
                                    } else if (property.dataTypeEnum == 'Email') {
                                        if (item[c.field] == undefined || item[c.field].indexOf(columnFilters[columnId]) == -1) {
                                            return false;
                                        }
                                    } else {
                                        alert('Unsupported dataType ' + property.dataTypeEnum);
                                    }
                                } else if (!property.onePrimitive && !property.manyPrimitive && !property.composite) {
                                    if (item[c.field].displayName == undefined &&  item[c.field] != columnFilters[columnId]) {
                                        return false;
                                    }
                                    if (item[c.field].displayName !== undefined && item[c.field].displayName.indexOf(columnFilters[columnId]) == -1) {
                                        return false;
                                    }
                                } else if (property.fieldType == 'String') {
                                    if (item[c.field] !== undefined && item[c.field].indexOf(columnFilters[columnId]) == -1) {
                                        return false;
                                    }
                                } else if (property.fieldType == 'Integer' || property.fieldType == 'Long') {
                                    if (item[c.field] != columnFilters[columnId]) {
                                        return false;
                                    }
                                } else if (property.fieldType == 'Boolean') {
                                    var isNone = (columnFilters[columnId] === 'None');
                                    if (!isNone) {
                                        var isTrueSet = (columnFilters[columnId] === 'true');
                                        if (item[c.field] != isTrueSet) {
                                            return false;
                                        }
                                    }
                                }   
                            }
                        }
                    }
                }
                return true;
            }

            $(".grid-header .ui-icon").addClass("ui-state-default ui-corner-all").mouseover(function(e) {
                $(e.target).addClass("ui-state-hover")
            }).mouseout(function(e) {
                $(e.target).removeClass("ui-state-hover")
            });

            grid.onValidationError.subscribe(function (e, args) {
                alert(args.validationResults.msg);
            });

            // initialize the model after all the events have been hooked up
            dataView.beginUpdate();
            dataView.setItems(data);
            dataView.setFilterArgs({
                metaForData: metaForData
            });
            dataView.setFilter(filter);
            dataView.endUpdate();
            updateHeaderRow(metaForData);

            // if you don't want the items that are not visible (due to being filtered out
            // or being on a different page) to stay selected, pass 'false' to the second arg
            dataView.syncGridSelection(grid, true);

            $("#gridContainer").resizable();
        }
        init();
    }


    function selectEditor(property) {
        if (property.name == 'uri') {
            return null;
        } else if (property.dataTypeEnum !== undefined) {
            if (property.dataTypeEnum == 'Date') {
                return  Tuml.Slick.Editors.Date; 
            } else if (property.dataTypeEnum == 'Time') {
                return  Tuml.Slick.Editors.Time; 
            } else if (property.dataTypeEnum == 'DateTime') {
                return  Tuml.Slick.Editors.DateTime; 
            } else if (property.dataTypeEnum == 'InternationalPhoneNumber') {
                return null; 
            } else if (property.dataTypeEnum == 'LocalPhoneNumber') {
                return null; 
            } else if (property.dataTypeEnum == 'Email') {
                return null; 
            } else if (property.dataTypeEnum == 'Video') {
                return null; 
            } else  if (property.dataTypeEnum == 'Audio') {
                return null; 
            } else if (property.dataTypeEnum == 'Image') {
                return null; 
            } else {
                alert('Unsupported dataType ' + property.dataTypeEnum);
            }
        } else if (property.oneEnumeration) {
            return  Tuml.Slick.Editors.SelectEnumerationCellEditor; 
        } else if (!property.onePrimitive && !property.manyPrimitive && !property.composite) {
            return  Tuml.Slick.Editors.SelectCellEditor; 
        } else if (property.name == 'id') {
            return null;
        } else if (property.fieldType == 'String') {
            return Tuml.Slick.Editors.Text; 
        } else if (property.fieldType == 'Integer') {
            return Tuml.Slick.Editors.Integer;
        } else if (property.fieldType == 'Long') {
            return Slick.Editors.Integer;
        } else if (property.fieldType == 'Boolean') {
            return Tuml.Slick.Editors.Checkbox;
        } else {
            return  Slick.Editors.Text; 
        }
    }

    function selectFieldValidator(property) {
        if (property.name == 'uri') {
        } else if (property.dataTypeEnum !== undefined) {
            if (property.dataTypeEnum == 'Date') {
            } else if (property.dataTypeEnum == 'Time') {
            } else if (property.dataTypeEnum == 'DateTime') {
            } else if (property.dataTypeEnum == 'InternationalPhoneNumber') {
            } else if (property.dataTypeEnum == 'LocalPhoneNumber') {
            } else if (property.dataTypeEnum == 'Email') {
            } else if (property.dataTypeEnum == 'Video') {
            } else  if (property.dataTypeEnum == 'Audio') {
            } else if (property.dataTypeEnum == 'Image') {
            } else {
                alert('Unsupported dataType ' + property.dataTypeEnum);
            }
        } else if (!property.onePrimitive && !property.manyPrimitive && !property.composite) {
        } else if (property.name == 'id') {
        } else if (property.fieldType == 'String') {
            return new TumlSlick.Validators.TumlString(property).validate;
        } else if (property.fieldType == 'Integer') {
            return new TumlSlick.Validators.TumlNumber(property).validate;
        } else if (property.fieldType == 'Long') {
            return new TumlSlick.Validators.TumlNumber(property).validate;
        } else if (property.fieldType == 'Boolean') {
        } else {
        }
        return function() {
            return {
                valid: true,
                msg: null}
        };
    }

    function selectFormatter(property) {
        if (property.name == 'uri') {
            return TumlSlick.Formatters.Link;
        } else if (property.name == 'id') {
            return TumlSlick.Formatters.Id;
        } else if (property.dataTypeEnum !== undefined) {
            return null;
        } else if (property.oneEnumeration) {
            return  TumlSlick.Formatters.TumlRequired; 
        } else if (!property.onePrimitive && !property.manyPrimitive) {
            return function waitingFormatter(row, cell, value, columnDef, dataContext) {
                if (value !== undefined && value !== null) {
                    return value.displayName;
                } else {
                    return '';
                }
            };
            return null;
        } else if (property.fieldType == 'String') {
            return  TumlSlick.Formatters.TumlRequired; 
        } else if (property.fieldType == 'Boolean') {
            return Slick.Formatters.Checkmark;
        } else {
            return null; 
        }
    }

})(jQuery);
