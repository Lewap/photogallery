// src/main/resources/static/js/script.js
document.addEventListener('DOMContentLoaded', function() {
    // Add any interactive JavaScript functionality here
    console.log('Photo Gallery loaded');

    // Handle checkbox selection
    const checkboxes = document.querySelectorAll('.photo-checkbox');
    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const photoCard = this.closest('.photo-card');
            if (this.checked) {
                photoCard.style.backgroundColor = '#e3f2fd';
            } else {
                photoCard.style.backgroundColor = '';
            }
            updateSelectedPhotos();
        });
    });

    // Update UI when selection changes
    function updateSelectedPhotos() {
        const selectedCount = document.querySelectorAll('.photo-checkbox:checked').length;
    }

    // Get IDs of selected photos
    function getSelectedPhotoIds() {
        const selected = [];
        document.querySelectorAll('.photo-checkbox:checked').forEach(checkbox => {
            selected.push(checkbox.dataset.photoId);
        });
        return selected;
    }

    const tagButton = document.getElementById('tag-button');
    if (tagButton) {
        tagButton.addEventListener('click', function() {
            const selectedIds = getSelectedPhotoIds();
            if (selectedIds.length > 0) {
                if (confirm(`Are you sure you want to tag ${selectedIds.length} photo(s)?`)) {
                    const form = document.createElement('form');
                    form.method = 'POST';
                    form.action = '/api/tagging/tag-selected';

                    // Add CSRF token if needed (this is a simplified version)
                    selectedIds.forEach(id => {
                        const input = document.createElement('input');
                        input.type = 'hidden';
                        input.name = 'ids';
                        input.value = id;
                        form.appendChild(input);
                    });

                    const providerSelect = document.getElementById('provider-select');
                    const provider = providerSelect.value;
                    const providerInput = document.createElement('input');
                    providerInput.type = 'hidden';
                    providerInput.name = 'provider';
                    providerInput.value = provider;
                    form.appendChild(providerInput);

                    const modelSelect = document.getElementById('model-select');
                    const model = modelSelect.value;
                    const modelInput = document.createElement('input');
                    modelInput.type = 'hidden';
                    modelInput.name = 'model';
                    modelInput.value = model;
                    form.appendChild(modelInput);

                    document.body.appendChild(form);
                    form.submit();
                }
            } else {
                alert('Please select at least one photo to tag');
            }
        });
    }
    // Handle provider selection change to load available models
    const providerSelect = document.getElementById('provider-select');
    const modelSelect = document.getElementById('model-select');
    const searchPrompt = document.getElementById('search-input');

    if (providerSelect && modelSelect) {
        // Initialize the model select state based on initial provider selection
        updateModelSelectState();

        providerSelect.addEventListener('change', function() {
            updateModelSelectState();

            const selectedProvider = this.value;

            // Only fetch models if a valid provider is selected
            if (selectedProvider && selectedProvider !== '') {
                // Clear current options
                modelSelect.innerHTML = '';

                // Add loading option
                const loadingOption = document.createElement('option');
                loadingOption.textContent = 'Loading. ..';
                loadingOption.disabled = true;
                modelSelect.appendChild(loadingOption);

                // Fetch available models
                fetch(`/api/tagging/available-models?provider=${selectedProvider}-models`)
                    .then(response => response.json())
                    .then(models => {
                        // Clear the loading option
                        modelSelect.innerHTML = '';

                        // Add default option
                        const defaultOption = document.createElement('option');
                        defaultOption.textContent = 'Select a model';
                        defaultOption.value = '';
                        defaultOption.disabled = true;
                        defaultOption.selected = true;
                        modelSelect.appendChild(defaultOption);

                        // Add available models
                        models.forEach(model => {
                            const option = document.createElement('option');
                            option.textContent = model;
                            option.value = model;
                            modelSelect.appendChild(option);
                        });
                    })
                    .catch(error => {
                        console.error('Error fetching models:', error);
                        // Clear the loading option and show error
                        modelSelect.innerHTML = '';
                        const errorOption = document.createElement('option');
                        errorOption.textContent = 'Failed to load models';
                        errorOption.disabled = true;
                        modelSelect.appendChild(errorOption);
                    });
            }
        });
    }

    // Helper function to update model select state based on provider selection
    function updateModelSelectState() {
        const providerSelect = document.getElementById('provider-select');
        const modelSelect = document.getElementById('model-select');

        if (providerSelect && modelSelect) {
            const selectedProvider = providerSelect.value;

            if (!selectedProvider || selectedProvider === '') {
                // Disable model select and show placeholder text
                modelSelect.disabled = true;
                modelSelect.innerHTML = '<option>Select a provider first</option>';
            } else {
                // Enable model select
                modelSelect.disabled = false;
                // Clear existing options but keep the loading state
                modelSelect.innerHTML = '';
                const loadingOption = document.createElement('option');
                loadingOption.textContent = 'Loading. ..';
                loadingOption.disabled = true;
                modelSelect.appendChild(loadingOption);
            }
        }
    }

    function updateTagButtonState() {
        const tagButton = document.getElementById('tag-button');
        const selectedCount = document.querySelectorAll('.photo-checkbox:checked').length;
        const providerSelect = document.getElementById('provider-select');
        const modelSelect = document.getElementById('model-select');

        if (tagButton && providerSelect && modelSelect) {
            // Check if a valid provider is selected (not default)
            const providerSelected = providerSelect.value && providerSelect.value !== '';

            // Check if a valid model is selected (not default)
            const modelSelected = modelSelect.value && modelSelect.value !== '';

            // Enable button only when all conditions are met
            tagButton.disabled = selectedCount === 0 || !providerSelected || !modelSelected;
        }
    }

    function updateSearchButtonState() {
        const searchButton = document.getElementById('search-button');
        const searchPrompt = document.getElementById('search-input');
        const providerSelect = document.getElementById('provider-select');
        const modelSelect = document.getElementById('model-select');

        if (searchButton && providerSelect && modelSelect && searchPrompt) {
            // Check if a valid provider is selected (not default)
            const providerSelected = providerSelect.value && providerSelect.value !== '';

            // Check if a valid model is selected (not default)
            const modelSelected = modelSelect.value && modelSelect.value !== '';

            const searchNotEmpty = searchPrompt.value && searchPrompt.value !== '';

            // Enable button only when all conditions are met
            searchButton.disabled = !providerSelected || !modelSelected || !searchNotEmpty;
        }
    }

    // Add event listeners to provider and model selects to update button state
    if (providerSelect) {
        providerSelect.addEventListener('change', updateTagButtonState);
        providerSelect.addEventListener('change', updateSearchButtonState);
    }

    if (modelSelect) {
        modelSelect.addEventListener('change', updateTagButtonState);
        modelSelect.addEventListener('change', updateSearchButtonState);
    }

    if (searchPrompt) {
        searchPrompt.addEventListener('input', updateSearchButtonState);
        searchPrompt.addEventListener('change', updateSearchButtonState);
    }

    // Also update button state when checkboxes change
    document.querySelectorAll('.photo-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', updateTagButtonState);
    });

    const searchButton = document.getElementById('search-button');
    if (searchButton) {
        searchButton.addEventListener('click', function() {
            const form = document.createElement('form');
            form.method = 'GET';
            form.action = '/api/search/search';

            const providerSelect = document.getElementById('provider-select');
            const provider = providerSelect.value;
            const providerInput = document.createElement('input');
            providerInput.type = 'hidden';
            providerInput.name = 'provider';
            providerInput.value = provider;
            form.appendChild(providerInput);

            const modelSelect = document.getElementById('model-select');
            const model = modelSelect.value;
            const modelInput = document.createElement('input');
            modelInput.type = 'hidden';
            modelInput.name = 'model';
            modelInput.value = model;
            form.appendChild(modelInput);

            const searchPrompt = document.getElementById('search-input').value;
            const searchInput = document.createElement('input');
            searchInput.type = 'hidden';
            searchInput.name = 'searchPrompt';
            searchInput.value = searchPrompt;
            form.appendChild(searchInput);

            document.body.appendChild(form);
            form.submit();
        });
    }

});