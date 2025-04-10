// Add or update the PlayMusic component to handle image URLs better

// Function to check if a string is a data URI
const isDataUri = (str) => {
  return str && typeof str === 'string' && str.startsWith('data:') && str.includes(';base64,');
};

// Create a helper function to safely use image URLs
const renderImage = (imageUrl, altText) => {
  // Log the URL for debugging
  console.log('Rendering image with URL:', imageUrl);
  
  if (!imageUrl) {
    return <div className="fallback-image">{altText ? altText.charAt(0).toUpperCase() : '♪'}</div>;
  }
  
  // For data URIs, make sure they're properly formatted
  if (isDataUri(imageUrl)) {
    console.log('Detected data URI image');
    // Data URIs should be used directly
    return (
      <img 
        src={imageUrl} 
        alt={altText || 'Music cover'} 
        onError={(e) => {
          console.error('Error loading data URI image:', e);
          e.target.style.display = 'none';
          e.target.nextElementSibling.style.display = 'block';
        }}
      />
    );
  }
  
  // For regular URLs, load normally
  return (
    <img 
      src={imageUrl} 
      alt={altText || 'Music cover'} 
      onError={(e) => {
        console.error('Error loading image URL:', imageUrl);
        e.target.style.display = 'none';
        e.target.nextElementSibling.style.display = 'block';
      }}
    />
  );
};

// Use the function in the component where you render images 