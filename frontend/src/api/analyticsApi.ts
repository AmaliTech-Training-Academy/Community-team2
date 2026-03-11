import type { Analytics } from "../types";
import { buildAnalytics } from "./communityApi";

export const analyticsApi = {
  get: (): Promise<Analytics> => buildAnalytics(),
};
