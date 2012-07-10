Selenium.prototype.doTypeRepeated = function(locator, text) {
	// All locator-strategies are automatically handled by "findElement"
	var element = this.page().findElement(locator);

	// Create the text to type
	var valueToType = text + text;

	// Replace the element text with the new text
	this.page().replaceText(element, valueToType);
};