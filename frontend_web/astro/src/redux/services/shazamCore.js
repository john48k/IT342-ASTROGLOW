import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

export const shazamCoreApi = createApi({
  reducerPath: "shazamCoreApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "https://shazam-core7.p.rapidapi.com/",
    prepareHeaders: (headers) => {
      headers.set(
        "X-RapidAPI-Key",
        import.meta.env.VITE_SHAZAM_CORE_RAPID_API_KEY ||
          "5f4e5ac0a3mshc04b79b6d5344a5p1ab1eejsnb9fd583022dd"
      );
      headers.set("X-RapidAPI-Host", "shazam-core7.p.rapidapi.com");
      return headers;
    },
  }),
  endpoints: (builder) => ({
    getTopCharts: builder.query({
      query: () => ({
        url: "charts/get-top-songs-in-world",
        params: { limit: "10" },
      }),
    }),
    getSongsByGenre: builder.query({
      query: (genre) => ({
        url: "charts/get-top-songs-in-genre-world",
        params: { genre_code: genre, limit: "10" },
      }),
    }),
    getSongsByCountry: builder.query({
      query: (countryCode) => ({
        url: "charts/get-top-songs-in-country",
        params: { country_code: countryCode, limit: "10" },
      }),
    }),
    getSongsBySearch: builder.query({
      query: (searchTerm) => ({
        url: "search",
        params: { term: searchTerm, limit: "10" },
      }),
    }),
    getArtistDetails: builder.query({
      query: (artistId) => ({
        url: "artist/get-details",
        params: { id: artistId },
      }),
    }),
    getSongDetails: builder.query({
      query: ({ songid }) => ({
        url: "songs/get-details",
        params: { id: songid },
      }),
    }),
    getSongRelated: builder.query({
      query: ({ songid }) => ({
        url: "songs/get-related-artist",
        params: { id: songid, limit: "10" },
      }),
    }),
  }),
});

export const {
  useGetTopChartsQuery,
  useGetSongsByGenreQuery,
  useGetSongsByCountryQuery,
  useGetSongsBySearchQuery,
  useGetArtistDetailsQuery,
  useGetSongDetailsQuery,
  useGetSongRelatedQuery,
} = shazamCoreApi;
