import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

// const options = {
//   method: "GET",
//   headers: {
//     "x-rapidapi-key": "5f4e5ac0a3mshc04b79b6d5344a5p1ab1eejsnb9fd583022dd",
//     "x-rapidapi-host": "shazam-core7.p.rapidapi.com",
//   },
// };

// fetch(
//   "https://shazam-core7.p.rapidapi.com/charts/get-top-songs-in-world",
//   options
// )
//   .then((response) => response.json())
//   .then((response) => console.log(response))
//   .catch((err) => console.error(err));

export const shazamCoreApi = createApi({
  reducerPath: "shazamCoreApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "https://shazam-core7.p.rapidapi.com/v1",
    prepareHeaders: () => {
      headers.set(
        "x-rapidapi-key",
        "5f4e5ac0a3mshc04b79b6d5344a5p1ab1eejsnb9fd583022dd"
      );
      return headers;
    },
  }),
  endpoints: (builder) => ({
    getTopCharts: builder.query({ query: () => "/charts/world" }),
  }),
});

export const { useGetTopChartsQuery } = shazamCoreApi;
