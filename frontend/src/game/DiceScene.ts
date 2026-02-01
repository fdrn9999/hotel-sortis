import * as THREE from 'three'
import * as CANNON from 'cannon-es'
import type { DiceSkin } from '@/types/game'

export interface DiceResult {
  die1: number
  die2: number
  die3: number
}

export class DiceScene {
  private scene: THREE.Scene
  private camera: THREE.PerspectiveCamera
  private renderer: THREE.WebGLRenderer
  private world: CANNON.World

  private dice: THREE.Mesh[] = []
  private diceBodies: CANNON.Body[] = []

  private animationId: number | null = null
  private container: HTMLElement

  private isRolling = false
  private onRollComplete: ((result: DiceResult) => void) | null = null
  private targetResult: DiceResult | null = null

  private currentSkin: DiceSkin | null = null

  constructor(container: HTMLElement) {
    this.container = container

    // Three.js 씬 초기화
    this.scene = new THREE.Scene()
    this.scene.background = new THREE.Color(0x1b1b27) // CLAUDE.md에서 짙은 남색

    // 카메라 설정
    this.camera = new THREE.PerspectiveCamera(
      45,
      container.clientWidth / container.clientHeight,
      0.1,
      1000
    )
    this.camera.position.set(0, 5, 10)
    this.camera.lookAt(0, 0, 0)

    // 렌더러 설정
    this.renderer = new THREE.WebGLRenderer({ antialias: true })
    this.renderer.setSize(container.clientWidth, container.clientHeight)
    this.renderer.setPixelRatio(window.devicePixelRatio)
    this.renderer.shadowMap.enabled = true
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap
    container.appendChild(this.renderer.domElement)

    // Cannon.js 물리 세계 초기화
    this.world = new CANNON.World()
    this.world.gravity.set(0, -9.82, 0) // 중력
    this.world.broadphase = new CANNON.NaiveBroadphase()
    ;(this.world.solver as any).iterations = 10

    this.setupLights()
    this.setupTable()
    this.createDice()

    // 리사이즈 핸들러
    window.addEventListener('resize', this.handleResize.bind(this))

    // 애니메이션 시작
    this.animate()
  }

  private setupLights(): void {
    // 앰비언트 라이트
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.5)
    this.scene.add(ambientLight)

    // 디렉셔널 라이트 (샹들리에 조명 느낌)
    const directionalLight = new THREE.DirectionalLight(0xfffdd0, 1) // 크림색
    directionalLight.position.set(5, 10, 5)
    directionalLight.castShadow = true
    directionalLight.shadow.camera.left = -10
    directionalLight.shadow.camera.right = 10
    directionalLight.shadow.camera.top = 10
    directionalLight.shadow.camera.bottom = -10
    directionalLight.shadow.mapSize.width = 2048
    directionalLight.shadow.mapSize.height = 2048
    this.scene.add(directionalLight)

    // 포인트 라이트 (금빛 반사)
    const pointLight = new THREE.PointLight(0xd4af37, 0.5, 20) // 금색
    pointLight.position.set(0, 5, 0)
    this.scene.add(pointLight)
  }

  private createDiceFaceTextures(skin?: DiceSkin): THREE.MeshStandardMaterial[] {
    const materials: THREE.MeshStandardMaterial[] = []
    const faceIndices = [4, 2, 1, 6, 3, 5] // 각 면의 번호 순서 (right, left, top, bottom, front, back)

    // 스킨 속성 (기본값 또는 스킨에서 가져옴)
    const baseColor = skin?.baseColor || '#FFFDD0' // 기본: 상아색
    const pipColor = skin?.pipColor || '#000000'   // 기본: 검은색
    const roughness = skin?.roughness ?? 0.5
    const metalness = skin?.metalness ?? 0.1
    const emissiveColor = skin?.emissiveColor
    const emissiveIntensity = skin?.emissiveIntensity ?? 0

    for (const faceValue of faceIndices) {
      // Canvas 생성 (256x256)
      const canvas = document.createElement('canvas')
      canvas.width = 256
      canvas.height = 256
      const ctx = canvas.getContext('2d')!

      // 배경 (스킨 색상)
      ctx.fillStyle = baseColor
      ctx.fillRect(0, 0, 256, 256)

      // 눈금 (스킨 색상)
      ctx.fillStyle = pipColor

      const pipRadius = 16
      const pipPositions = this.getPipPositions(faceValue)

      for (const pos of pipPositions) {
        ctx.beginPath()
        ctx.arc(pos.x * 256, pos.y * 256, pipRadius, 0, Math.PI * 2)
        ctx.fill()
      }

      // Canvas를 Texture로 변환
      const texture = new THREE.CanvasTexture(canvas)
      texture.needsUpdate = true

      // Material 생성 (스킨 속성 적용)
      const materialOptions: THREE.MeshStandardMaterialParameters = {
        map: texture,
        roughness,
        metalness
      }

      // Emissive (발광) 효과 적용
      if (emissiveColor && emissiveIntensity > 0) {
        materialOptions.emissive = new THREE.Color(emissiveColor)
        materialOptions.emissiveIntensity = emissiveIntensity
      }

      const material = new THREE.MeshStandardMaterial(materialOptions)

      materials.push(material)
    }

    return materials
  }

  private getPipPositions(value: number): { x: number; y: number }[] {
    const center = { x: 0.5, y: 0.5 }
    const topLeft = { x: 0.3, y: 0.3 }
    const topRight = { x: 0.7, y: 0.3 }
    const middleLeft = { x: 0.3, y: 0.5 }
    const middleRight = { x: 0.7, y: 0.5 }
    const bottomLeft = { x: 0.3, y: 0.7 }
    const bottomRight = { x: 0.7, y: 0.7 }

    switch (value) {
      case 1:
        return [center]
      case 2:
        return [topLeft, bottomRight]
      case 3:
        return [topLeft, center, bottomRight]
      case 4:
        return [topLeft, topRight, bottomLeft, bottomRight]
      case 5:
        return [topLeft, topRight, center, bottomLeft, bottomRight]
      case 6:
        return [topLeft, topRight, middleLeft, middleRight, bottomLeft, bottomRight]
      default:
        return [center]
    }
  }

  private setupTable(): void {
    // 테이블 (나무 탁자)
    const tableGeometry = new THREE.BoxGeometry(8, 0.5, 6)
    const tableMaterial = new THREE.MeshStandardMaterial({
      color: 0x8b4513, // 갈색 나무
      roughness: 0.8,
      metalness: 0.2
    })
    const table = new THREE.Mesh(tableGeometry, tableMaterial)
    table.position.set(0, -0.25, 0)
    table.receiveShadow = true
    this.scene.add(table)

    // 물리 바닥
    const groundShape = new CANNON.Box(new CANNON.Vec3(4, 0.25, 3))
    const groundBody = new CANNON.Body({
      mass: 0, // 정적 객체
      shape: groundShape,
      material: new CANNON.Material({ friction: 0.5, restitution: 0.3 })
    })
    groundBody.position.set(0, -0.25, 0)
    this.world.addBody(groundBody)
  }

  private createDice(skin?: DiceSkin): void {
    // 주사위 면 텍스처 생성 (스킨 적용)
    const diceMaterials = this.createDiceFaceTextures(skin)

    // 3개의 주사위 생성
    for (let i = 0; i < 3; i++) {
      // Three.js 주사위 (시각적)
      const diceGeometry = new THREE.BoxGeometry(0.16, 0.16, 0.16) // 16mm
      const dice = new THREE.Mesh(diceGeometry, diceMaterials)
      dice.castShadow = true
      dice.receiveShadow = true

      // 초기 위치 (테이블 위)
      dice.position.set(i * 0.5 - 0.5, 2, 0)
      this.scene.add(dice)
      this.dice.push(dice)

      // Cannon.js 주사위 (물리)
      const diceShape = new CANNON.Box(new CANNON.Vec3(0.08, 0.08, 0.08))
      const diceBody = new CANNON.Body({
        mass: 0.1, // 0.1kg
        shape: diceShape,
        material: new CANNON.Material({ friction: 0.5, restitution: 0.3 })
      })
      diceBody.position.set(i * 0.5 - 0.5, 2, 0)
      this.world.addBody(diceBody)
      this.diceBodies.push(diceBody)
    }

    // 현재 스킨 저장
    this.currentSkin = skin || null
  }

  private animate(): void {
    this.animationId = requestAnimationFrame(() => this.animate())

    // 물리 시뮬레이션 업데이트
    this.world.step(1 / 60)

    // Three.js 오브젝트를 Cannon.js 위치/회전에 동기화
    for (let i = 0; i < 3; i++) {
      this.dice[i].position.copy(this.diceBodies[i].position as any)
      this.dice[i].quaternion.copy(this.diceBodies[i].quaternion as any)
    }

    // 서버 결과 동기화 (rollTo 사용 시)
    if (this.isRolling && this.targetResult) {
      // 주사위가 충분히 느려졌을 때 목표 회전으로 보정
      for (let i = 0; i < 3; i++) {
        const body = this.diceBodies[i]
        const speed = body.velocity.length() + body.angularVelocity.length()

        // 속도가 0.5 이하로 떨어지면 보정 시작
        if (speed < 0.5 && body.position.y < 0.5) {
          const targetValue = i === 0 ? this.targetResult.die1 :
                             i === 1 ? this.targetResult.die2 :
                             this.targetResult.die3

          const targetQuat = this.getQuaternionForFace(targetValue)

          // 현재 quaternion을 목표 quaternion으로 부드럽게 전환
          body.quaternion.copy(targetQuat as any)
          body.angularVelocity.set(0, 0, 0) // 회전 멈춤
          body.velocity.set(0, 0, 0) // 이동 멈춤
        }
      }
    }

    // 주사위가 멈췄는지 확인
    if (this.isRolling) {
      const allStopped = this.diceBodies.every(body =>
        body.velocity.length() < 0.1 &&
        body.angularVelocity.length() < 0.1
      )

      if (allStopped) {
        this.isRolling = false
        // targetResult가 있으면 서버 결과 사용, 없으면 물리 결과 사용
        const result = this.targetResult || this.getDiceResult()
        if (this.onRollComplete) {
          this.onRollComplete(result)
        }
        this.targetResult = null // 초기화
      }
    }

    // 렌더링
    this.renderer.render(this.scene, this.camera)
  }

  public roll(onComplete: (result: DiceResult) => void): void {
    if (this.isRolling) return

    this.isRolling = true
    this.onRollComplete = onComplete
    this.targetResult = null // 자유 굴림

    // 주사위를 테이블 위로 리셋
    for (let i = 0; i < 3; i++) {
      const body = this.diceBodies[i]

      // 위치 리셋
      body.position.set(
        Math.random() * 2 - 1, // -1 ~ 1
        3 + Math.random(), // 3 ~ 4
        Math.random() * 2 - 1
      )

      // 랜덤 회전
      body.quaternion.setFromEuler(
        Math.random() * Math.PI * 2,
        Math.random() * Math.PI * 2,
        Math.random() * Math.PI * 2
      )

      // 랜덤 속도
      body.velocity.set(
        (Math.random() - 0.5) * 5,
        -2,
        (Math.random() - 0.5) * 5
      )

      // 랜덤 회전 속도
      body.angularVelocity.set(
        (Math.random() - 0.5) * 10,
        (Math.random() - 0.5) * 10,
        (Math.random() - 0.5) * 10
      )
    }
  }

  public rollTo(targetValues: DiceResult, onComplete: (result: DiceResult) => void): void {
    if (this.isRolling) return

    this.isRolling = true
    this.onRollComplete = onComplete
    this.targetResult = targetValues // 서버 결과 저장

    // 물리 시뮬레이션 시작 (roll()과 동일)
    for (let i = 0; i < 3; i++) {
      const body = this.diceBodies[i]

      // 위치 리셋
      body.position.set(
        Math.random() * 2 - 1,
        3 + Math.random(),
        Math.random() * 2 - 1
      )

      // 랜덤 회전
      body.quaternion.setFromEuler(
        Math.random() * Math.PI * 2,
        Math.random() * Math.PI * 2,
        Math.random() * Math.PI * 2
      )

      // 랜덤 속도
      body.velocity.set(
        (Math.random() - 0.5) * 5,
        -2,
        (Math.random() - 0.5) * 5
      )

      // 랜덤 회전 속도
      body.angularVelocity.set(
        (Math.random() - 0.5) * 10,
        (Math.random() - 0.5) * 10,
        (Math.random() - 0.5) * 10
      )
    }
  }

  private getQuaternionForFace(faceValue: number): CANNON.Quaternion {
    // 각 면의 회전값 (주사위를 회전시켜 해당 면이 위를 향하게)
    const rotations: { [key: number]: { x: number; y: number; z: number } } = {
      1: { x: 0, y: 0, z: 0 },                    // 위
      6: { x: Math.PI, y: 0, z: 0 },             // 아래
      2: { x: 0, y: 0, z: -Math.PI / 2 },        // 오른쪽
      5: { x: 0, y: 0, z: Math.PI / 2 },         // 왼쪽
      3: { x: Math.PI / 2, y: 0, z: 0 },         // 앞
      4: { x: -Math.PI / 2, y: 0, z: 0 }         // 뒤
    }

    const rot = rotations[faceValue]
    const quat = new CANNON.Quaternion()
    quat.setFromEuler(rot.x, rot.y, rot.z)
    return quat
  }

  private getDiceResult(): DiceResult {
    // 주사위의 위쪽 면을 판정
    const results: number[] = []

    for (const body of this.diceBodies) {
      // 월드 좌표계에서 주사위의 위쪽 방향 벡터
      const up = new CANNON.Vec3(0, 1, 0)

      // 주사위의 6개 면 벡터
      const faces = [
        new CANNON.Vec3(0, 1, 0),  // 위 (면 1)
        new CANNON.Vec3(0, -1, 0), // 아래 (면 6)
        new CANNON.Vec3(1, 0, 0),  // 오른쪽 (면 2)
        new CANNON.Vec3(-1, 0, 0), // 왼쪽 (면 5)
        new CANNON.Vec3(0, 0, 1),  // 앞 (면 3)
        new CANNON.Vec3(0, 0, -1)  // 뒤 (면 4)
      ]

      // 각 면의 번호
      const faceValues = [1, 6, 2, 5, 3, 4]

      // 가장 위를 향하는 면 찾기
      let maxDot = -Infinity
      let faceIndex = 0

      for (let i = 0; i < faces.length; i++) {
        const faceWorld = body.quaternion.vmult(faces[i])
        const dot = faceWorld.dot(up)

        if (dot > maxDot) {
          maxDot = dot
          faceIndex = i
        }
      }

      results.push(faceValues[faceIndex])
    }

    return {
      die1: results[0],
      die2: results[1],
      die3: results[2]
    }
  }

  private handleResize(): void {
    const width = this.container.clientWidth
    const height = this.container.clientHeight

    this.camera.aspect = width / height
    this.camera.updateProjectionMatrix()

    this.renderer.setSize(width, height)
  }

  /**
   * 주사위 스킨 변경 (런타임)
   * CLAUDE.md Phase 9 - 코스메틱 시스템
   */
  public applySkin(skin: DiceSkin): void {
    console.log(`Applying dice skin: ${skin.name} (${skin.skinCode})`)

    // 새 재질 생성
    const newMaterials = this.createDiceFaceTextures(skin)

    // 각 주사위에 새 재질 적용
    for (const dice of this.dice) {
      // 기존 재질 정리
      if (Array.isArray(dice.material)) {
        dice.material.forEach(mat => mat.dispose())
      } else {
        dice.material.dispose()
      }

      // 새 재질 적용
      dice.material = newMaterials
    }

    // 현재 스킨 저장
    this.currentSkin = skin

    console.log(`Dice skin applied: baseColor=${skin.baseColor}, pipColor=${skin.pipColor}, metalness=${skin.metalness}, roughness=${skin.roughness}`)
  }

  /**
   * 현재 적용된 스킨 가져오기
   */
  public getCurrentSkin(): DiceSkin | null {
    return this.currentSkin
  }

  public dispose(): void {
    // 애니메이션 중지
    if (this.animationId !== null) {
      cancelAnimationFrame(this.animationId)
    }

    // 리사이즈 핸들러 제거
    window.removeEventListener('resize', this.handleResize.bind(this))

    // Three.js 객체 정리
    this.dice.forEach(dice => {
      dice.geometry.dispose()
      if (Array.isArray(dice.material)) {
        dice.material.forEach(mat => mat.dispose())
      } else {
        dice.material.dispose()
      }
    })

    this.scene.clear()
    this.renderer.dispose()

    // DOM에서 canvas 제거
    if (this.renderer.domElement.parentElement) {
      this.renderer.domElement.parentElement.removeChild(this.renderer.domElement)
    }
  }
}
