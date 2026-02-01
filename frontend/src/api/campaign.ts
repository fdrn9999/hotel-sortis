import apiClient from './client'
import type {
  CampaignProgressResponse,
  StartFloorResponse,
  FloorCompleteResponse,
  SkillRewardOption
} from '@/types/game'

export const campaignApi = {
  async getCampaignProgress(playerId: number): Promise<CampaignProgressResponse> {
    const response = await apiClient.get<CampaignProgressResponse>(
      `/campaigns/${playerId}`
    )
    return response.data
  },

  async startFloor(
    playerId: number,
    floor: number,
    equippedSkillIds: number[]
  ): Promise<StartFloorResponse> {
    const response = await apiClient.post<StartFloorResponse>(
      `/campaigns/${playerId}/floors/${floor}/start`,
      { playerId, equippedSkillIds }
    )
    return response.data
  },

  async completeFloorBattle(
    battleId: number,
    playerId: number
  ): Promise<FloorCompleteResponse> {
    const response = await apiClient.post<FloorCompleteResponse>(
      `/campaigns/battles/${battleId}/complete?playerId=${playerId}`
    )
    return response.data
  },

  async getSkillRewards(
    playerId: number,
    floor: number
  ): Promise<SkillRewardOption[]> {
    const response = await apiClient.get<SkillRewardOption[]>(
      `/campaigns/${playerId}/floors/${floor}/rewards`
    )
    return response.data
  },

  async selectSkillReward(
    playerId: number,
    floor: number,
    selectedSkillId: number
  ): Promise<void> {
    await apiClient.post(
      `/campaigns/${playerId}/floors/${floor}/rewards`,
      { playerId, selectedSkillId }
    )
  }
}

export default campaignApi
