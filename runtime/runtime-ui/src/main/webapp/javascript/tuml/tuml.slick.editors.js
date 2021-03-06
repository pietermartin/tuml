/***
 * Contains basic SlickGrid editors.
 * @module Editors
 * @namespace Slick
 */

(function ($) {
    // register namespace
    $.extend(true, window, {
        "Tuml": {
            "Slick": {
                "Editors": {
                    "Integer": IntegerEditor,
                    "Double": DoubleEditor,
                    "Text": TextEditor,
                    "SelectOneToOneCellEditor": SelectOneToOneCellEditor,
                    "SelectToOneAssociationClassCellEditor": SelectToOneAssociationClassCellEditor,
                    "SelectEnumerationCellEditor": SelectEnumerationCellEditor,
                    "ManyPrimitiveEditor": ManyPrimitiveEditor,
                    "ManyStringPrimitiveEditor": ManyStringPrimitiveEditor,
                    "ManyEnumerationEditor": ManyEnumerationEditor,
                    "ManyIntegerPrimitiveEditor": ManyIntegerPrimitiveEditor,
                    "ManyDoublePrimitiveEditor": ManyDoublePrimitiveEditor,
                    "ManyBooleanPrimitiveEditor": ManyBooleanPrimitiveEditor,
                    "Checkbox": CheckboxEditor,
                    "ManyDateEditor": ManyDateEditor,
                    "ManyTime": ManyTimeEditor,
                    "ManyDateTime": ManyDateTimeEditor,
                    "Date": DateEditor,
                    "DateTime": DateTimeEditor,
                    "Time": TimeEditor
                }
            }}
    });

    function TextEditor(args) {
        //Public api
        $.extend(this, {
            "TumlTextEditor": "1.0.0",
            "serializeValueWithValue": serializeValueWithValue
        });
        var $input;
        var defaultValue;

        this.init = function () {
            $input = $("<TEXTAREA type=" + (args.column.options.property.dataTypeEnum === 'Password' || args.column.options.property.dataTypeEnum === 'UnsecurePassword' ? "'password'" : "'text'") + "class='slick-grid-umlg-editor-text' ></TEXTAREA>")
                .appendTo(args.container)
                .bind("keydown.nav", function (e) {
                    if (e.keyCode === $.ui.keyCode.LEFT || e.keyCode === $.ui.keyCode.RIGHT) {
                        e.stopImmediatePropagation();
                    }
                })
                .focus()
                .select();
        };

        this.destroy = function () {
            $input.remove();
        };

        this.focus = function () {
            $input.focus();
        };

        this.getValue = function () {
            return $input.val();
        };

        this.setValue = function (val) {
            $input.val(val);
        };

        this.loadValue = function (item) {
            defaultValue = item[args.column.field] || null;
            $input.val(defaultValue);
            $input[0].defaultValue = defaultValue;
            $input.select();
        };

        this.serializeValue = function () {
            return serializeValueWithValue($input);
        };

        function serializeValueWithValue(input) {
            var value = input.val();
            if (value == '') {
                return null;
            } else {
                return value;
            }
        };

        this.applyValue = function (item, state) {
            item[args.column.field] = state;
        };

        this.isValueChanged = function () {
            return ($input.val() != defaultValue);
        };

        this.validate = function () {
            if (args.column.validator) {
                var validationResults = args.column.validator($input.val());
                if (!validationResults.valid) {
                    return validationResults;
                }
            }

            return {
                valid: true,
                msg: null
            };
        };

        //This is called from the grid, the one only uses the serializeValueWithValue function
        if (args !== undefined) {
            this.init();
        }
    }

    function ManyPrimitiveEditor(args) {
        this.args = args;
        this.$input;
        this.$table;
        var defaultValue;

        this.handleKeyPress = function (e) {
            if (e.target == this.$input[0]) {
                if (e.which == 13) {
                    this.addButton.click();
                    e.stopImmediatePropagation();
                }
            }
        }

        this.destroy = function () {
            this.args.grid['manyPrimitiveEditorOpen'] = false;
            this.panel.remove();
        };

        this.focus = function () {
            this.$input.focus();
        };

        this.loadValue = function (item) {
            //defaultValue must be an array
            defaultValue = item[this.args.column.field];
            if (defaultValue !== undefined && defaultValue !== null) {
                for (var i = 0; i < defaultValue.length; i++) {
                    this.addTr(defaultValue[i]);
                }
                if (this.args.column.options.ordered) {
                    this.$table.tableDnD();
                }
            }
        };

        this.addTr = function (value) {
            var row = $('<tr />').addClass('many-primitive-editor-row');
            var rowValue = $('<td class="many-primitive-editor-cell" />').text(value);
            row.append(rowValue);
            row.data('value', value);
            var img = $('<img class="umlg-many-select-img" src="/' + tumlModelName + '/javascript/images/delete.png">').click(function () {
                var liClicked = $(this).parent().parent();
                liClicked.remove();
            });
            var imgValue = $('<td class="many-primitive-editor-cell many-primitive-editor-cell-img" />');
            imgValue.append(img);
            row.append(imgValue);
            this.$table.append(row);
        }

        this.applyValue = function (item, state) {
            item[this.args.column.field] = state;
        };

        this.isValueChanged = function () {
            var rowArray = this.$table.find('.many-primitive-editor-row');
            var arrayToSerialize = [];
            for (var i = 0; i < rowArray.length; i++) {
                var row = rowArray[i];
                arrayToSerialize.push($(row).data('value'));
            }
            var result = false;
            if (defaultValue !== undefined && defaultValue !== null && defaultValue.length == arrayToSerialize.length) {
                for (var i = 0; i < arrayToSerialize.length; i++) {
                    if (defaultValue[i] != arrayToSerialize[i]) {
                        result = true;
                        break;
                    }
                }
            } else {
                result = true;
            }
            return result;
        };

        this.validate = function () {
            if (this.args.column.validator) {
                var rowArray = this.$table.find('.many-primitive-editor-row');
                if (rowArray.length > 0) {
                    var arrayToSerialize = [];
                    for (var i = 0; i < rowArray.length; i++) {
                        var row = rowArray[i];
                        arrayToSerialize.push($(row).data('value'));
                    }
                    var validationResults = this.args.column.validator(arrayToSerialize);
                    if (!validationResults.valid) {
                        return validationResults;
                    }
                }
            }

            return {
                valid: true,
                msg: null
            };
        };
        //This is called from the grid, the one only uses the serializeValueWithValue function
        if (this.args !== undefined) {
            this.init();
        }
    }

    ManyPrimitiveEditor.prototype.serializeEditorValue = function (value) {
        alert('serializeEditorValue must be overriden');
    };
    ManyPrimitiveEditor.prototype.serializeValue = function () {
        alert('this must be overriden');
    };
    ManyPrimitiveEditor.prototype.createInput = function (div) {
        var input = $('<input type=text class="many-primitive-editor-input">');
        div.append(input);
        return input;
    }
    ManyPrimitiveEditor.prototype.resetInput = function (input) {
        input.val('');
    }
    ManyPrimitiveEditor.prototype.init = function () {
        if (this.args !== undefined) {
            this.args.grid['manyPrimitiveEditorOpen'] = true;
            this.args.grid['manyPrimitiveEditor'] = this;
        }
        this.panel = $("<div class='umlg-panel-many-editor panel panel-default many-primitive-editor' />");
        var panelHeading = $('<div class="panel-heading umlg-panel-heading-popup" />').appendTo(this.panel);
        var topNavBar = $('<div class="collapse navbar-collapse" />').appendTo(panelHeading);
        var panelBody = $('<div class="panel-body" />').appendTo(this.panel);

        var self = this;
        this.addButton = $('<button type="button" id="many-primitive-editor-input-add-button" class="btn btn-default pull-left" />').text('Add').click(function () {
            var valueToAdd = self.$input.val();
            var currentValues = self.serializeValue();
            var testArray = [];
            testArray.push(valueToAdd);
            var validationResults = self.args.column.validator(testArray);
            if (currentValues.length !== 0 && validationResults.valid && self.args.column.options.unique) {
                var serializedValueToAdd = self.serializeEditorValue(valueToAdd);
                validationResults = self.args.column.validator(currentValues, serializedValueToAdd);
            }
            if (!validationResults.valid) {
                alert(validationResults.msg);
            } else {
                self.addTr(valueToAdd);
                if (self.args.column.options.ordered) {
                    self.$table.tableDnD();
                }
                self.resetInput(self.$input);
            }
        }).appendTo(topNavBar);
        var formGroup = $('<div class="form-group pull-left" />')
        this.$input = this.createInput(formGroup);
        this.$input.addClass("form-control");
        formGroup.appendTo(topNavBar);

        var resultDiv = $('<div class="many-primitive-editor-result" />').appendTo(panelBody);
        this.$table = $('<table class="table table-bordered many-primitive-editor-result-table" />').appendTo(resultDiv);

        this.panel.bind("keydown.nav", function (e) {
            if (e.keyCode === $.ui.keyCode.LEFT || e.keyCode === $.ui.keyCode.RIGHT) {
                e.stopImmediatePropagation();
            }
        });

        this.panel.appendTo(this.args.container);
        this.$input.focus().select();
    };

    function ManyStringPrimitiveEditor(args) {
        this.args = args;
        if (this.args !== undefined) {
            this.init();
        }
    }

    ManyStringPrimitiveEditor.prototype = new Tuml.Slick.Editors.ManyPrimitiveEditor();
    ManyStringPrimitiveEditor.prototype.serializeEditorValue = function (value) {
        return value;
    };
    ManyStringPrimitiveEditor.prototype.serializeValueForOne = function (table) {
        var rowArray = table.find('.many-primitive-editor-row');
        var arrayToSerialize = [];
        for (var i = 0; i < rowArray.length; i++) {
            var row = rowArray[i];
            arrayToSerialize.push($(row).data('value'));
        }
        return arrayToSerialize;
    }
    ManyStringPrimitiveEditor.prototype.serializeValue = function () {
        var rowArray = this.$table.find('.many-primitive-editor-row');
        var arrayToSerialize = [];
        for (var i = 0; i < rowArray.length; i++) {
            var row = rowArray[i];
            arrayToSerialize.push($(row).data('value'));
        }
        return arrayToSerialize;
    }

    ManyStringPrimitiveEditor.prototype.serializeValueWithValue = function () {
        alert('ManyStringPrimitiveEditor.prototype.serializeValueWithValue must be overriden!');
    }

    function ManyEnumerationEditor(args) {
        this.args = args;
        if (this.args !== undefined) {
            this.$item = args.item;
            this.init();
        }
    }

    ManyEnumerationEditor.prototype = new Tuml.Slick.Editors.ManyStringPrimitiveEditor();
    ManyEnumerationEditor.prototype.createInput = function (parentDiv) {
        var $select = $("<SELECT id='aaaaaaaaaId' tabIndex='0' class='editor-select many-primitive-editor-input'></SELECT>");
        $select.appendTo(parentDiv);
        var $self = this;
        this.args.column.options.rowEnumerationLookupMap.getOrLoadMap(function (data) {
            if (!$self.args.column.options.required) {
                $select.append($('<option />)').val("").html(""));
            }
            $.each(data, function (index, obj) {
                $select.append($('<option />)').val(obj).html(obj));
            });
            var currentValue = $self.$item[$self.args.column.field];
            $select.val(currentValue);
            if (!$self.args.column.options.required) {
                $select.chosen({allow_single_deselect: true});
            } else {
                $select.chosen();
            }
            $select.focus();
        });
        return $select;
    }
    ManyEnumerationEditor.prototype.resetInput = function (input) {
        input.val('').trigger('liszt:updated');
    }

    function ManyDateEditor(args) {
        this.args = args;
        if (this.args !== undefined) {
            this.$item = args.item;
            this.init();
        }
    }

    ManyDateEditor.prototype = new Tuml.Slick.Editors.ManyStringPrimitiveEditor;
    ManyDateEditor.prototype.createInput = function (div) {
        var input = $("<INPUT type=text />");
        input.appendTo(div);
        input.focus().select();
        input.datepicker({
            showOn: "button",
            buttonImageOnly: true,
            buttonImage: "../javascript/slickgrid/images/calendar.gif",
            dateFormat: "yy-mm-dd",
            beforeShow: function () {
                calendarOpen = true
            },
            onClose: function () {
                calendarOpen = false
            }
        });
        return input;
    }

    function ManyTimeEditor(args) {

        this.args = args;
        if (this.args !== undefined) {
            this.$item = args.item;
            this.init();
        }
    }

    ManyTimeEditor.prototype = new Tuml.Slick.Editors.ManyStringPrimitiveEditor;
    ManyTimeEditor.prototype.createInput = function (div) {
        var input = $("<INPUT type=text />");
        input.appendTo(div);
        input.focus().select();
        input.timepicker({
            showOn: "button",
            buttonImageOnly: true,
            buttonImage: "../javascript/slickgrid/images/calendar.gif",
            beforeShow: function () {
                calendarOpen = true
            },
            onClose: function () {
                calendarOpen = false
            }
        });
        return input;
    }

    function ManyDateTimeEditor(args) {
        this.args = args;
        if (this.args !== undefined) {
            this.$item = args.item;
            this.init();
        }
    }

    ManyDateTimeEditor.prototype = new Tuml.Slick.Editors.ManyStringPrimitiveEditor;
    ManyDateTimeEditor.prototype.createInput = function (div) {
        var input = $("<INPUT type=text />");
        input.appendTo(div);
        input.focus().select();
        input.datetimepicker({
            showOn: "button",
            buttonImageOnly: true,
            buttonImage: "../javascript/slickgrid/images/calendar.gif",
            dateFormat: "yy-mm-dd",
            timeFormat: "hh:mm:ss",
            beforeShow: function () {
                calendarOpen = true
            },
            onClose: function () {
                calendarOpen = false
            }
        });
        return input;
    }

    function ManyDoublePrimitiveEditor(args) {
        this.args = args;
        //Only the from the grid is init called
        if (this.args !== undefined) {
            this.init();
        }
    }

    ManyDoublePrimitiveEditor.prototype = new Tuml.Slick.Editors.ManyPrimitiveEditor;
    ManyDoublePrimitiveEditor.prototype.serializeEditorValue = function (value) {
        return parseFloat(value, 10);
    };
    ManyDoublePrimitiveEditor.prototype.serializeValueForOne = function (table) {
        var rowArray = table.find('.many-primitive-editor-row');
        var arrayToSerialize = [];
        for (var i = 0; i < rowArray.length; i++) {
            var row = rowArray[i];
            arrayToSerialize.push(parseFloat($(row).data('value'), 10));
        }
        return arrayToSerialize;
    }
    ManyDoublePrimitiveEditor.prototype.serializeValue = function () {
        var rowArray = this.$table.find('.many-primitive-editor-row');
        var arrayToSerialize = [];
        for (var i = 0; i < rowArray.length; i++) {
            var row = rowArray[i];
            arrayToSerialize.push(parseFloat($(row).data('value'), 10));
        }
        return arrayToSerialize;
    }

    function ManyIntegerPrimitiveEditor(args) {
        this.args = args;
        //Only the from the grid is init called
        if (this.args !== undefined) {
            this.init();
        }
    }

    ManyIntegerPrimitiveEditor.prototype = new Tuml.Slick.Editors.ManyPrimitiveEditor;
    ManyIntegerPrimitiveEditor.prototype.serializeEditorValue = function (value) {
        return parseInt(value, 10);
    };
    ManyIntegerPrimitiveEditor.prototype.serializeValueForOne = function (table) {
        var rowArray = table.find('.many-primitive-editor-row');
        var arrayToSerialize = [];
        for (var i = 0; i < rowArray.length; i++) {
            var row = rowArray[i];
            arrayToSerialize.push(parseInt($(row).data('value'), 10));
        }
        return arrayToSerialize;
    }
    ManyIntegerPrimitiveEditor.prototype.serializeValue = function () {
        var rowArray = this.$table.find('.many-primitive-editor-row');
        var arrayToSerialize = [];
        for (var i = 0; i < rowArray.length; i++) {
            var row = rowArray[i];
            arrayToSerialize.push(parseInt($(row).data('value'), 10));
        }
        return arrayToSerialize;
    }

    function ManyBooleanPrimitiveEditor(args) {
        this.args = args;
        if (this.args !== undefined) {
            this.init();
        }
    }

    ManyBooleanPrimitiveEditor.prototype = new Tuml.Slick.Editors.ManyPrimitiveEditor();
    ManyBooleanPrimitiveEditor.prototype.serializeEditorValue = function (value) {
        var valueTrue = (value === 'true') || (value === 't') || (value === '1');
        var valueFalse = (value === 'false') || (value === 'f') || (value === '0');
        if (valueTrue) {
            return true;
        } else if (valueFalse) {
            return false;
        } else {
            //This should only be called on a already validated value
            throw 'Incorrect value for ManyBooleanPrimitiveEditor.prototype.serializeEditorValue: ' + value;
        }
    };
    ManyBooleanPrimitiveEditor.prototype.serializeValueForOne = function (table) {
        var rowArray = table.find('.many-primitive-editor-row');
        var arrayToSerialize = [];
        for (var i = 0; i < rowArray.length; i++) {
            var row = rowArray[i];
            var value = $(row).data('value');
            var isTrueSet = value === 'true' || value === 't' || value === '1';
            arrayToSerialize.push(isTrueSet);
        }
        return arrayToSerialize;
    }
    ManyBooleanPrimitiveEditor.prototype.serializeValue = function () {
        var rowArray = this.$table.find('.many-primitive-editor-row');
        var arrayToSerialize = [];
        for (var i = 0; i < rowArray.length; i++) {
            var row = rowArray[i];
            var value = $(row).data('value');
            var isTrueSet = value === 'true' || value === 't' || value === '1' || value;
            arrayToSerialize.push(isTrueSet);
        }
        return arrayToSerialize;
    }

    function IntegerEditor(args) {
        //Public api
        $.extend(this, {
            "TumlIntegerEditor": "1.0.0",
            "serializeValueWithValue": serializeValueWithValue
        });
        var $input;
        var defaultValue;
        var scope = this;

        this.init = function () {
            $input = $("<INPUT type=text class='slick-grid-umlg-editor-text' />");

            $input.bind("keydown.nav", function (e) {
                if (e.keyCode === $.ui.keyCode.LEFT || e.keyCode === $.ui.keyCode.RIGHT) {
                    e.stopImmediatePropagation();
                }
            });

            $input.appendTo(args.container);
            $input.focus().select();
        };

        this.destroy = function () {
            $input.remove();
        };

        this.focus = function () {
            $input.focus();
        };

        this.loadValue = function (item) {
            defaultValue = item[args.column.field];
            $input.val(defaultValue);
            $input[0].defaultValue = defaultValue;
            $input.select();
        };

        this.serializeValue = function () {
            return serializeValueWithValue($input);
        };

        function serializeValueWithValue(input) {
            return parseInt(input.val(), 10) || 0;
        }

        this.applyValue = function (item, state) {
            item[args.column.field] = state;
        };

        this.isValueChanged = function () {
            return (!($input.val() == "" && defaultValue == null)) && ($input.val() != defaultValue);
        };

        this.validate = function () {
            if (args.column.validator) {
                var validationResults = args.column.validator($input.val());
                if (!validationResults.valid) {
                    return validationResults;
                }
            }

            return {
                valid: true,
                msg: null
            };
        };

        //This is called from the grid, the one only uses the serializeValueWithValue function
        if (args !== undefined) {
            this.init();
        }
    }

    function DoubleEditor(args) {
        //Public api
        $.extend(this, {
            "TumlDoubleEditor": "1.0.0",
            "serializeValueWithValue": serializeValueWithValue
        });
        var $input;
        var defaultValue;

        this.init = function () {
            $input = $("<INPUT type=text class='slick-grid-umlg-editor-text' />");

            $input.bind("keydown.nav", function (e) {
                if (e.keyCode === $.ui.keyCode.LEFT || e.keyCode === $.ui.keyCode.RIGHT) {
                    e.stopImmediatePropagation();
                }
            });

            $input.appendTo(args.container);
            $input.focus().select();
        };

        this.destroy = function () {
            $input.remove();
        };

        this.focus = function () {
            $input.focus();
        };

        this.loadValue = function (item) {
            defaultValue = item[args.column.field];
            $input.val(defaultValue);
            $input[0].defaultValue = defaultValue;
            $input.select();
        };

        this.serializeValue = function () {
            return serializeValueWithValue($input);
        };

        function serializeValueWithValue(input) {
            return parseFloat(input.val()) || 0;
        }

        this.applyValue = function (item, state) {
            item[args.column.field] = state;
        };

        this.isValueChanged = function () {
            return (!($input.val() == "" && defaultValue == null)) && ($input.val() != defaultValue);
        };

        this.validate = function () {
            if (args.column.validator) {
                var validationResults = args.column.validator($input.val());
                if (!validationResults.valid) {
                    return validationResults;
                }
            }

            return {
                valid: true,
                msg: null
            };
        };

        //This is called from the grid, the one only uses the serializeValueWithValue function
        if (args !== undefined) {
            this.init();
        }
    }

    function DateEditor(args) {
        //Public api
        $.extend(this, {
            "TumlDateEditor": "1.0.0",
            "serializeValueWithValue": serializeValueWithValue
        });
        var $input;
        var defaultValue;

        this.init = function () {
            var divInputGroup = $('<div class="input-group date slick-grid-umlg-editor-text" data-date-format="YYYY-MM-DD" />');
            divInputGroup.appendTo(args.container);
            $input = $("<INPUT type='text' class='form-control slick-grid-umlg-editor-text' />");
            $input.appendTo(divInputGroup);
            var span = $('<span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>');
            span.appendTo(divInputGroup);
            divInputGroup.datetimepicker({pickTime: false});
        };

        this.destroy = function () {
            $input.parent().data('DateTimePicker').hide();
        };

        this.show = function () {
            $input.parent().data('DateTimePicker').show();
        };

        this.hide = function () {
            $input.parent().data('DateTimePicker').hide();
        };

        this.focus = function () {
            if (!$input.is(":hidden")) {
                $input.focus();
            }
        };

        this.loadValue = function (item) {
            defaultValue = item[args.column.field];
            if (defaultValue !== null) {
                $input.parent().data('DateTimePicker').setDate(defaultValue);
            }
            $input.parent().data('DateTimePicker').show();
        };

        this.serializeValue = function () {
            return serializeValueWithValue($input);
        };

        function serializeValueWithValue(input) {
            return input.parent().data('DateTimePicker').getDate().format('YYYY-MM-DD');
        }

        this.applyValue = function (item, state) {
            item[args.column.field] = state;
        };

        this.isValueChanged = function () {
            return (!($input.val() == "" && defaultValue == null)) && ($input.val() != defaultValue);
        };


        this.validate = function () {
            if (args.column.validator) {
                var validationResults = args.column.validator($input.val());
                if (!validationResults.valid) {
                    return validationResults;
                }
            }

            return {
                valid: true,
                msg: null
            };
        };

        //This is called from the grid, the one only uses the serializeValueWithValue function
        if (args !== undefined) {
            this.init();
        }
    }

    function DateTimeEditor(args) {
        //Public api
        $.extend(this, {
            "TumlDateTimeEditor": "1.0.0",
            "serializeValueWithValue": serializeValueWithValue
        });
        var $input;
        var defaultValue;

        this.init = function () {
            var divInputGroup = $('<div class="input-group date slick-grid-umlg-editor-text" data-date-format="YYYY-MM-DD HH:mm" />');
            divInputGroup.appendTo(args.container);
            $input = $("<INPUT type='text' class='form-control slick-grid-umlg-editor-text' />");
            $input.appendTo(divInputGroup);
            var span = $('<span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>');
            span.appendTo(divInputGroup);
            divInputGroup.datetimepicker();
        };

        this.destroy = function () {
            $input.parent().data('DateTimePicker').hide();
        };

        this.show = function () {
            $input.parent().data('DateTimePicker').show();
        };

        this.hide = function () {
            $input.parent().data('DateTimePicker').hide();
        };

        this.focus = function () {
            $input.focus();
        };

        this.loadValue = function (item) {
            defaultValue = item[args.column.field];
            if (defaultValue !== null) {
                $input.parent().data('DateTimePicker').setDate(defaultValue);
            }
            $input.parent().data('DateTimePicker').show();
        };

        this.serializeValue = function () {
            //$input.unmask();
            return serializeValueWithValue($input);
        };

        function serializeValueWithValue(input) {
            return input.parent().data('DateTimePicker').getDate().format('YYYY-MM-DD HH:mm:ss');
        }

        this.applyValue = function (item, state) {
            item[args.column.field] = state;
        };

        this.isValueChanged = function () {
            return (!($input.val() == "" && defaultValue == null)) && ($input.val() != defaultValue);
        };


        this.validate = function () {
            if (args.column.validator) {
                var validationResults = args.column.validator($input.val());
                if (!validationResults.valid) {
                    return validationResults;
                }
            }

            return {
                valid: true,
                msg: null
            };
        };

        //This is called from the grid, the one only uses the serializeValueWithValue function
        if (args !== undefined) {
            this.init();
        }
    }


    function TimeEditor(args) {
        //Public api
        $.extend(this, {
            "TumlTimeEditor": "1.0.0",
            "serializeValueWithValue": serializeValueWithValue
        });
        var $input;
        var defaultValue;

        this.init = function () {
            var divInputGroup = $('<div class="input-group date slick-grid-umlg-editor-text" data-date-format="HH:mm" />');
            divInputGroup.appendTo(args.container);
            $input = $("<INPUT type='text' class='form-control slick-grid-umlg-editor-text' />");
            $input.appendTo(divInputGroup);
            var span = $('<span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>');
            span.appendTo(divInputGroup);
            divInputGroup.datetimepicker({pickDate: false});
        };

        this.destroy = function () {
            $input.parent().data('DateTimePicker').hide();
        };

        this.show = function () {
            $input.parent().data('DateTimePicker').show();
        };

        this.hide = function () {
            $input.parent().data('DateTimePicker').hide();
        };

        this.focus = function () {
            $input.focus();
        };

        this.loadValue = function (item) {
            defaultValue = item[args.column.field];
            if (defaultValue !== null) {
                $input.parent().data('DateTimePicker').setDate(defaultValue);
            }
            $input.parent().data('DateTimePicker').show();
        };

        this.serializeValue = function () {
            return serializeValueWithValue($input);
        };

        function serializeValueWithValue(input) {
            return input.parent().data('DateTimePicker').getDate().format('HH:mm');
        }

        this.applyValue = function (item, state) {
            item[args.column.field] = state;
        };

        this.isValueChanged = function () {
            return (!($input.val() == "" && defaultValue == null)) && ($input.val() != defaultValue);
        };

        this.validate = function () {
            if (args.column.validator) {
                var validationResults = args.column.validator($input.parent().data('DateTimePicker').getDate());
                if (!validationResults.valid) {
                    return validationResults;
                }
            }

            return {
                valid: true,
                msg: null
            };
        };

        //This is called from the grid, the one only uses the serializeValueWithValue function
        if (args !== undefined) {
            this.init();
        }
    }

    function CheckboxEditor(args) {
        //Public api
        $.extend(this, {
            "TumlCheckBoxEditor": "1.0.0",
            "serializeValueWithValue": serializeValueWithValue
        });
        var $select;
        var defaultValue;

        this.init = function () {
            $select = $("<INPUT type=checkbox value='true' class='editor-checkbox' hideFocus>");
            $select.appendTo(args.container);
            $select.focus();
        };

        this.destroy = function () {
            $select.remove();
        };

        this.focus = function () {
            $select.focus();
        };

        this.loadValue = function (item) {
            defaultValue = item[args.column.field];
            if (defaultValue) {
                $select.attr("checked", "checked");
            } else {
                $select.removeAttr("checked");
            }
        };

        this.serializeValue = function () {
            return serializeValueWithValue($select);
        };

        function serializeValueWithValue(select) {
            return $select.is(':checked');
        }

        this.applyValue = function (item, state) {
            item[args.column.field] = state;
        };

        this.isValueChanged = function () {
            if ($select.is(':checked') && defaultValue == false) {
                return true;
            } else if (!$select.is(':checked') && defaultValue == true) {
                return true;
            } else {
                return false;
            }
        };

        this.validate = function () {
            return {
                valid: true,
                msg: null
            };
        };

        //This is called from the grid, the one only uses the serializeValueWithValue function
        if (args !== undefined) {
            this.init();
        }
    }

    function SelectEnumerationCellEditor(args) {
        //Public api
        $.extend(this, {
            "TumlSelectEnumerationCellEditor": "1.0.0",
            "serializeValueWithValue": serializeValueWithValue
        });
        var $select;
        var currentValue;

        this.init = function (item) {
            $select = $("<SELECT tabIndex='0' class='editor-select' style='width:115px;'></SELECT>");
            $select.appendTo(args.container);

            args.column.options.rowEnumerationLookupMap.getOrLoadMap(function (data) {
                if (!args.column.options.required) {
                    $select.append($('<option />)').val("").html(""));
                }
                $.each(data, function (index, obj) {
                    $select.append($('<option />)').val(obj).html(obj));
                });
                currentValue = item[args.column.field];
                $select.val(currentValue);
                if (!args.column.options.required) {
                    $select.chosen({allow_single_deselect: true});
                } else {
                    $select.chosen();
                }
                $select.focus();
            });

        };

        this.destroy = function () {
            $select.remove();
        };

        this.focus = function () {
            $select.focus();
        };

        this.loadValue = function (item) {
        };

        this.serializeValue = function () {
            if (args.column.options) {
                return serializeValueWithValue($select);
            } else {
                alert('why is this happening?');
                //return serializeValueWithValue($select.val() == "yes"));
            }
        };

        function serializeValueWithValue(select) {
            return select.val();
        }

        this.applyValue = function (item, state) {
            item[args.column.field] = state;
        };

        this.isValueChanged = function () {
            if (currentValue === null || currentValue === undefined) {
                return true;
            } else {
                return ($select.val() != currentValue);
            }
        };


        this.validate = function () {
            if (args.column.validator) {
                var validationResults = args.column.validator($select.val());
                if (!validationResults.valid) {
                    return validationResults;
                }
            }

            return {
                valid: true,
                msg: null
            };
        };


        //This is called from the grid, the one only uses the serializeValueWithValue function
        if (args !== undefined) {
            this.init(args.item);
        }
    }

    function SelectOneToOneCellEditor(args) {
        //Public api
        $.extend(this, {
            "TumlSelectOneToOneEditor": "1.0.0",
            "serializeValueWithValue": serializeValueWithValue
        });
        var $select;
        var currentValue;

        this.init = function (item) {
            $select = $("<SELECT tabIndex='0' class='editor-select' style='width:115px;'></SELECT>");
            $select.appendTo(args.container);
            var lookupUri = args.column.options.property.tumlLookupUri;
            //Fetch the lookup data
            var id = item.id;
            var adjustedUri = lookupUri.replace(new RegExp("\{(\s*?.*?)*?\}", 'gi'), id);

            var tumlBaseGridManager = args.column.options.tumlBaseGridManager;

            var row = args.grid.getActiveCell().row;
            if (row >= args.grid.getDataLength()) {
                tumlBaseGridManager.handleAddNewRow();
            } else {
                tumlBaseGridManager.handleLookup(adjustedUri, item.qualifiedName,
                    function (data) {
                        if (!args.column.options.required) {
                            $select.append($('<option />)').val("").html(""));
                        }
                        for (var i = 0; i < data.length; i++) {
                            var obj = data[i];
                            $select.append($('<option />)').val(obj.id).html(obj.name));
                        }
                        //currentValue is the vertex id of the oneToOne or manyToOne
                        currentValue = item[args.column.field];
                        //append the current value to the dropdown
                        if (currentValue !== undefined && currentValue !== null) {
                            $select.append($('<option selected="selected"/>)').val(currentValue.id).html(currentValue.displayName));
                        }
                        if (!args.column.options.required) {
                            $select.chosen({allow_single_deselect: true});
                        } else {
                            $select.chosen();
                        }
                        $select.focus();
                    }
                );
            }
        };

        this.destroy = function () {
            $select.remove();
        };

        this.focus = function () {
            $select.focus();
        };

        this.loadValue = function (item) {
        };

        this.serializeValue = function () {
            if (args.column.options) {
                return serializeValueWithValue($select);
            } else {
                alert('why is this happening? 2');
                //return ($select.val() == "yes");
            }
        };

        function serializeValueWithValue(select) {
            var options = select.children();
            for (var i = 0; i < options.length; i++) {
                if (options[i].selected) {
                    var value = select.val();
                    if (value === '') {
                        return {id: null, displayName: null, previousId: currentValue.id};
                    } else {
                        return {id: select.val(), displayName: options[i].label, previousId: currentValue.id};
                    }
                    break;
                }
            }
        }

        this.applyValue = function (item, state) {
            item[args.column.field] = state;
        };

        this.isValueChanged = function () {
            if (currentValue === null || currentValue === undefined) {
                return true;
            } else {
                return ($select.val() != currentValue.id);
            }
        };


        this.validate = function () {
            if (args.column.validator) {
                var validationResults = args.column.validator($select.val());
                if (!validationResults.valid) {
                    return validationResults;
                }
            }

            return {
                valid: true,
                msg: null
            };
        };

        //This is called from the grid, the one only uses the serializeValueWithValue function
        if (args !== undefined) {
            this.init(args.item);
        }
    }

    function SelectToOneAssociationClassCellEditor(args) {
        //Public api
        $.extend(this, {
            "SelectToOneAssociationClassCellEditor": "1.0.0",
            "serializeValueWithValue": serializeValueWithValue
        });
        var $select;
        var currentValue;

        this.init = function (item) {
            $select = $("<SELECT tabIndex='0' class='editor-select' style='width:115px;'></SELECT>");
            $select.appendTo(args.container);
            var lookupUri = args.column.options.property.tumlLookupUri;
            //Fetch the lookup data
            var id = item.id;
            var adjustedUri = lookupUri.replace(new RegExp("\{(\s*?.*?)*?\}", 'gi'), id);


            var tumlBaseGridManager = args.column.options.tumlBaseGridManager;

            var row = args.grid.getActiveCell().row;
            if (row >= args.grid.getDataLength()) {
                tumlBaseGridManager.handleAddNewRow();
            } else {
                tumlBaseGridManager.handleLookup(adjustedUri, item.qualifiedName,
                    function (data) {
                        if (!args.column.options.required) {
                            $select.append($('<option />)').val("").html(""));
                        }
                        for (var i = 0; i < data.length; i++) {
                            var obj = data[i];
                            $select.append($('<option />)').val(obj.id).html(obj.name));
                        }
                        ;
                        //currentValue is the vertex id of the oneToOne or manyToOne
                        currentValue = item[args.column.field];
                        //append the current value to the dropdown
                        if (currentValue !== undefined && currentValue !== null) {
                            $select.append($('<option selected="selected"/>)').val(currentValue.id).html(currentValue.displayName));
                        }
                        if (!args.column.options.required) {
                            $select.chosen({allow_single_deselect: true});
                        } else {
                            $select.chosen();
                        }
                        $select.focus();
                    }
                );
            }
        };

        this.destroy = function () {
            $select.remove();
        };

        this.focus = function () {
            $select.focus();
        };

        this.loadValue = function (item) {
        };

        this.serializeValue = function () {
            if (args.column.options) {
                return serializeValueWithValue($select);
            } else {
                alert('why is this happening? 2');
                //return ($select.val() == "yes");
            }
        };

        function serializeValueWithValue(select) {
            var options = select.children();
            for (var i = 0; i < options.length; i++) {
                if (options[i].selected) {
                    var value = select.val();
                    if (value === '') {
                        return {id: null, displayName: null, previousId: currentValue.id};
//                    } else if (!isNaN(value)) {
//                        return {id: parseInt(value), displayName: options[i].label, previousId: currentValue.id};
                    } else {
                        return {id: select.val(), displayName: options[i].label, previousId: currentValue.id};
                    }
                    break;
                }
            }
        }

        //Need to unset the associationClass
        this.applyValue = function (item, state) {
            item[args.column.field] = state;
            if (item[args.column.field].id !== null) {
                var fakeId = 'fake::' + Tuml.TumlFakeIndex++;
                item[args.column.options.property.associationClassPropertyName] = {id: fakeId, tmpId: fakeId, displayName: null, refreshFromDb: true};
            } else {
                item[args.column.options.property.associationClassPropertyName] = {id: null, tmpId: null, displayName: null, refreshFromDb: false};
            }
        };

        this.isValueChanged = function () {
            if (currentValue === null || currentValue === undefined) {
                return true;
            } else {
                if ((currentValue.id === undefined || currentValue.id === null) && $select.val() == "") {
                    return false;
                } else {
                    return ($select.val() != currentValue.id);
                }
            }
        };


        this.validate = function () {
            if (args.column.validator) {
                var validationResults = args.column.validator($select.val());
                if (!validationResults.valid) {
                    return validationResults;
                }
            }

            return {
                valid: true,
                msg: null
            };
        };

        //This is called from the grid, the one only uses the serializeValueWithValue function
        if (args !== undefined) {
            this.init(args.item);
        }
    }

})
    (jQuery);
