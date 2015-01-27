/**
 * 此文件包括了和动态表单相关的所有操作， 此文件依赖于 gt_base.js。
 * @autho albert
 * @since 0.1
 */
 
 function FormConfig(templateId, serviceCategory) {
    this.templateId = templateId;
	this.serviceCategory = serviceCategory;
 }
 
 function CusForm(formConfig) {
    
	this.formConfig = formConfig;
	    
	/**
	  * String keyId: 编辑数据的主键
	  * Map customisedParams
	  */
	this.createEditingForm = function(keyId, customisedParams) {
	    
	}
	
	this.createNewForm = function(parentComp, customisedParams) {
	    var tempId = this.formConfig.templateId;
		if (tempId == null) {
		    throw new "模板ID不能为空。";
		}
	    var template = this.loadTemplate(tempId);
		if (template == null) {
		    throw new "未找到模板ID对应的模板。";
		}
		var EditingPanel ep = new EditingPanel(template, form, [], customisedParams, mode);
		ep.create(parentComp);		
	}
	
	/**
	  * 从服务器端加载模板数据
	  */
	this.loadTemplate = function (templateId, serviceCategory) {
	    var mockData = {templateEntity : {},
		                form : {
						    tableEntity : {},
							primaryKey : '',
							fields : [{},
							          {},
									  
							         ], // fields
							buttons: [],
							children:[],
							initialized:false
					    },
						tokenId : '1'};
		return mockData;
	}
	
	
	/**
	  * 从服务器端加载指定表单的数据
	  */
	this.loadData = function (template, form, keyId) {
	}
	
 }
 
 /**
  * 绘制编辑面板
  * TemplateVO template, FormVO form, List<FieldEntity> editingFields,
            Map initParams, EditingMode mode
  */
 function EditingPanel (template, form, editingFields,
            initParams, mode) {
	this.template = template;
	this.form = form;
	this.editingFields = editingFields;
	this.initParams = initParams;
	this.mode = mode;
	
    this.create = function(parentComp) {
	    // 创建一个网格布局
		
		for(var field in this.editingFields) {
		    this.createComp(field);
		}
	}
	
	/**
	 * 创建组件
	 */
	this.createComp = function (parentComp, field) {
	    Editor editor = EditorFactory.getEditor(field);
		var comp = editor.getComponent(field);
		parentComp.append(comp);
	}
 }
 
 /**
  * 根据组件类型创建编辑器实例
  */
 var EditorFactory = {
    getEditor : function(field) {
	    return new TextFieldEditor();
	}
 }
 
 function TextFieldEditor () {
    this.getComponent = function(field) {
	    var tf = jQuery("<input type='text' />").attr("id", id);
		return tf;
	}
 }
 
 /**
  * 绘制表格面板
  * TemplateVO template, FormVO form, List<FieldEntity> editingFields,
            Map initParams, EditingMode mode
  */
 function TablePanel (template, form, editingFields,
            initParams, mode) {
	this.template = template;
	this.form = form;
	this.editingFields = editingFields;
	this.initParams = initParams;
	this.mode = mode;
 
    this.create = function(parentComp) {
	    
	}
	
	/**
	 * 装载表格数据
	 */
	this.loadData = function() {
	}
 } 
 
 /**
  * 绘制按钮面板
  * final TemplateVO template, final FormVO form, final EditingMode mode
  */
 function ButtonsPanel (template, form, mode) {
	this.template = template;
	this.form = form;
	this.mode = mode;
 
    this.create = function(parentComp) {
	    
	}
 } 