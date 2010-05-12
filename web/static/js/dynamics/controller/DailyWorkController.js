/** */
var DailyWorkController = function(options) {
  this.model = null;
  this.options = {
    userId: null,
    workQueueElement: null,
    assignedStoriesElement: null,
    tasksWithoutStoryElement: null,
    onUserLoadUpdate: function() {}
  };
  jQuery.extend(this.options, options);
  
  this.init();
  this.initialize();

  window.pageController.setMainController(this);
};
DailyWorkController.prototype = new CommonController();

DailyWorkController.prototype.handleModelEvents = function(event) {
  if(event instanceof DynamicsEvents.NamedEvent) {
    var eventName = event.getEventName();
    if(eventName === "removedFromWorkQueue" || eventName === "addedToWorkQueue") {
      this.model.reloadWorkQueue(this.options.userId);
    }
  }
  //task oe/el changed
  if(event instanceof DynamicsEvents.MetricsEvent && event.getObject() instanceof TaskModel) {
    this.options.onUserLoadUpdate();
  }
  //task responsibles changed
  if(event instanceof DynamicsEvents.RelationUpdatedEvent && event.getObject() instanceof TaskModel && event.getRelation() === "user") {
    this.options.onUserLoadUpdate();
  }
};

DailyWorkController.prototype.initialize = function() {
  var me = this;
  ModelFactory.initializeFor(ModelFactory.initializeForTypes.dailyWork,
    this.options.userId,
    function(model) {
      me.model = model;
      me._paintLists();
    }
  );
};

DailyWorkController.prototype._paintLists = function() {
  this.tasksWithoutStoryController = new DailyWorkTasksWithoutStoryController(
      this.model, this.options.tasksWithoutStoryElement, this);
  this.assignedStoriesController = new DailyWorkStoryListController(this.model,
      this.options.assignedStoriesElement, this);
  this.workQueueController = new WorkQueueController(this.model,
      this.options.workQueueElement, this, { userId: this.options.userId });
};


