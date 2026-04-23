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

        // Enable/disable the tag button based on selection
        const tagButton = document.getElementById('tag-button');
        if (tagButton) {
            tagButton.disabled = selectedCount === 0;
        }

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
                    // Create a form for batch deletion
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
                    document.body.appendChild(form);
                    form.submit();
                }
            } else {
                alert('Please select at least one photo to tag');
            }
        });
    }

});