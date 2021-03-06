function registerSSEvent() {
  this.start = function () {
    document.getElementById("pagestate").innerHTML = "Order";
    this.source = new EventSource(`/receipt-sse/${sendToAddress}`);
    this.source.addEventListener("message", function (event) {
      $(".container-contact100-form-btn").hide();
      $(".contact50-form-title").hide();
      document.getElementById("pagestate").innerHTML = "Receipt";
      document.getElementById("name").innerHTML = JSON.parse(event.data).name;
    });
    
    this.source.onerror = function () {
      this.close();
    };
  };

  this.stop = function() {
    this.source.close();
  }
}

registerSSEvent = new registerSSEvent();

window.onload = function() {
  registerSSEvent.start();
}

window.onbeforeunload = function() {
  registerSSEvent.stop();
}